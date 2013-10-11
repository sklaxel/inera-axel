package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.mime.ShsMessage;

import java.io.InputStream;

/**
 * Saves the message body to the message log. Supports both an ShsMessage
 * and an InputStream body.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class SaveMessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(SaveMessageProcessor.class);

    @Handler
    public void saveMessage(@Body Object body, Exchange exchange) throws Exception {
        LOG.trace("Saving message");
        MessageLogService messageLogService = exchange.getContext().getRegistry().lookup("messageLogService", MessageLogService.class);

        if (body instanceof ShsMessage) {
            exchange.getIn().setBody(messageLogService.saveMessage((ShsMessage) body));
        } else {
            InputStream streamBody = exchange.getIn().getMandatoryBody(InputStream.class);
            exchange.getIn().setBody(messageLogService.saveMessageStream(streamBody));
        }
    }
}
