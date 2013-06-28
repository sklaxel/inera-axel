/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.ds.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.xml.TimestampAdapter;
import se.inera.axel.shs.xml.UrnAddress;
import se.inera.axel.shs.xml.UrnProduct;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.message.Data;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DeliveryServiceRouteBuilder extends RouteBuilder {

    MessageLogService messageLogService;

    public MessageLogService getMessageLogService() {
        return messageLogService;
    }

    public void setMessageLogService(MessageLogService messageLogService) {
        this.messageLogService = messageLogService;
    }

    @Override
    public void configure() throws Exception {

        onException(IllegalArgumentException.class)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_BAD_REQUEST))
        .transform(simple("${exception.message}"))
        .handled(true);


        from("jetty:{{shsDsHttpEndpoint}}:{{shsDsHttpEndpoint.port}}/shs/ds" +
                "?sslContextParametersRef=mySslContext" +
                "&enableJmx=true" +
                "&matchOnUriPrefix=true")
        .routeId("jetty:/shs/ds").tracing()
        .bean(new HttpPathParamsExtractor())
        .validate(header("outbox").isNotNull())
        .choice()
        .when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
                .to("direct:post")
        .otherwise()
                .to("direct:get");


        from("direct:get")
        .choice()
        .when(header("txId").isNotNull())
                .to("direct:fetchMessage")
        .otherwise()
                .to("direct:listMessages")
        .end();


        from("direct:fetchMessage")
        .onCompletion()
                .beanRef("messageLogService", "messageFetched(${property.entry})")
        .end()
        .beanRef("messageLogService", "findEntryByShsToAndTxid(${header.outbox}, ${header.txId})")
        .choice()
            .when(body().isNull())
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_NOT_FOUND))
                .stop()
            .end()
        .setProperty("entry", body())
        .beanRef("messageLogService", "fetchMessage(${property.entry})")
        .choice()
            .when(body().isNull())
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_NOT_FOUND))
                .stop()
            .end()
        .convertBodyTo(InputStream.class);


        from("direct:listMessages")
        .bean(new HeaderToFilterConverter())
        .beanRef("messageLogService", "listMessages(${header.outbox}, ${body})")
        .bean(new MessageListConverter())
        .convertBodyTo(String.class);


        from("direct:post")
        .choice()
        .when(header("action").isEqualTo("ack"))
               .to("direct:acknowledgeMessage");


        from("direct:acknowledgeMessage")
        .choice()
        .when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
        .beanRef("messageLogService", "findEntryByShsToAndTxid(${header.outbox}, ${header.txId})")
        .choice()
            .when(body().isNull())
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_NOT_FOUND))
                .stop()
            .end()
        .setProperty("entry", body())
        .beanRef("messageLogService", "acknowledge(${property.entry})")
        .choice()
            .when(body().isNull())
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_NOT_FOUND))
                .stop()
            .end();

    }

    public static class HttpPathParamsExtractor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {

            String httpPath = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
            String restPath = StringUtils.removeStart(httpPath, "/shs/ds/");

            UrnAddress outbox = UrnAddress.valueOf(StringUtils.substringBefore(restPath, "/"));
            exchange.getIn().setHeader("outbox", outbox.toSimpleForm());

            String txIdOrProductId = StringUtils.substringAfter(restPath, "/");

            if (StringUtils.isEmpty(txIdOrProductId)) {
                return;
            }

            try {
                // If its an UUID, assume it is the txId
                String txId = UUID.fromString(txIdOrProductId).toString();
                exchange.getIn().setHeader("txId", txId);
            } catch (Exception e) {
                try {
                    // else, assume it's the product id.
                    UrnProduct urnProduct = UrnProduct.valueOf(txIdOrProductId);
                    exchange.getIn().setHeader("producttype", urnProduct.getProductId());
                } catch (Exception ee) {
                    ;
                }
            }

        }
    }

    public static class MessageListConverter {

        public ShsMessageList toShsMessageList(Iterable<ShsMessageEntry> entries) {
            ShsMessageList messageList = new ShsMessageList();

            for (ShsMessageEntry entry : entries) {
                messageList.getMessage().add(toMessage(entry));
            }

            return messageList;
        }

        public Message toMessage(ShsMessageEntry entry) {
            Message message =
                    new se.inera.axel.shs.xml.message.Message();

            ShsLabel label = entry.getLabel();
            if (label.getProduct() != null)
                message.setProduct(label.getProduct().getvalue());

            if (label.getContent() != null)
                message.setContentId(label.getContent().getContentId());
            message.setCorrId(label.getCorrId());

            if (label.getEndRecipient() != null)
                message.setEndRecipient(label.getEndRecipient().getvalue());

            if (label.getFrom() != null)
                message.setFrom(label.getFrom().getvalue());

            if (label.getOriginator() != null)
                message.setOriginator(label.getOriginator().getvalue());

            message.setSequenceType(label.getSequenceType());
            // message.setSize();
            message.setStatus(label.getStatus());
            message.setSubject(label.getSubject());
            message.setTimestamp(label.getDatetime());

            if (label.getTo() != null)
                message.setTo(label.getTo().getvalue());

            message.setTxId(label.getTxId());

            for (Object object : label.getContent().getDataOrCompound()) {
                if (object instanceof se.inera.axel.shs.xml.label.Data) {
                    se.inera.axel.shs.xml.label.Data labelData = (se.inera.axel.shs.xml.label.Data)object;
                    Data data = new Data();
                    data.setDatapartType(labelData.getDatapartType());
                    data.setFilename(labelData.getFilename());
                    data.setNoOfBytes(labelData.getNoOfBytes());
                    data.setNoOfRecords(labelData.getNoOfRecords());
                    message.getData().add(data);
                }
            }



            return message;
        }

    }


    public static class HeaderToFilterConverter {

        public MessageLogService.Filter toFilter(
                @Header("producttype") String producttype,
                @Header("filter") String noAckfilter,
                @Header("maxhits") Integer maxHits,
                @Header("status") String status,
                @Header("corrid") String corrId,
                @Header("contentId") String contentId,
                @Header("originator") String originator,
                @Header("since") String since,
                @Header("sortattribute") String sortattribute,
                @Header("sortorder") String sortorder,
                @Header("arrivalorder") String arrivalorder,
                @Header("endrecipient") String endrecipient)
        {

            MessageLogService.Filter filter = new MessageLogService.Filter();


            if ("noack".equals(noAckfilter))
                filter.setNoAck(true);

            if (producttype != null) {
                List<String> productIds = Arrays.asList(StringUtils.split(producttype, ','));
                filter.setProductIds(productIds);
            }

            filter.setOriginator(originator);

            filter.setEndRecipient(endrecipient);

            filter.setMaxHits(maxHits);

            filter.setContentId(corrId);

            filter.setContentId(contentId);


            if (since != null) {
                SimpleDateFormat formatter = new SimpleDateFormat(TimestampAdapter.DATETIME_FORMAT);
                try {
                    filter.setSince(formatter.parse(since));
                } catch (Exception e) {
                    throw new IllegalArgumentException("timestamp format error on 'since': " + since);
                }
            }

            filter.setSortAttribute(sortattribute);

            filter.setArrivalOrder(arrivalorder);

            filter.setSortOrder(sortorder);
            if (status != null)
                filter.setStatus(Status.valueOf(status.toUpperCase()));

            return filter;
        }
    }
}
