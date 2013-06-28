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
package se.inera.axel.shs.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.camel.*;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.ShsUrn;
import se.inera.axel.shs.xml.label.SequenceType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Parameters(commandDescription = "Fetches a list of available messages from an SHS Server")
public class ShsFetchCommand extends ShsBaseCommand {

    @Parameter(names = {"-i", "--transactionId"}, description = "Transaction id of the message to fetch")
    public String shsTxId;

    @Parameter(names = {"-s", "--retrieveSingleFile"}, description = "If omitted all matching available files will be fetched")
    public boolean retrieveSingleFile;

    @Parameter(names = {"-o", "--originalFileNames"}, description = "If omitted the transaction id will be used in file names")
    public boolean originalFileNames;

    @Parameter(names = {"-T", "--onlyTestMessages"}, description = "Only fetch test messages")
    public boolean onlyTestMessages;

    @Parameter(names = {"-l", "--list"}, description = "Get list of messages")
    public boolean listMode;

    @Parameter(names = {"--noAck"}, description = "Only include non acknowledged messages")
    public boolean noAck;

    @Parameter(names = "--since", converter = ISO8601DateTimeConverter.class, description = "Date and time in ISO 8601 format yyyy-mm-ddThh:mm:ss. Only message that arrived later are included.")
    public DateTime since;

    @Parameter(names = {"-d", "--dir"}, description = "Path to the directory where the result should be stored")
    public String directoryName;

    @Parameter(names = {"-q", "--sequenceType"}, description = "The type of messages to fetch (event|request|reply|adm)")
    public SequenceType sequenceType;

    @Parameter(names = {"--maxHits"}, description = "Maximum number of messages to list or fetch")
    public Integer maxHits;

    @Parameter(names = {"-p", "--productTypes"} , variableArity = true, description = "The product types to fetch")
    public List<String> productTypes = new ArrayList<String>();

    public final void execute() throws Throwable {
        if (directoryName != null) {
            File outputDirFile = new File(directoryName);
            if (!outputDirFile.exists()) {
                throw new RuntimeException(String.format("Output directory %s does not exist or is a file", outputDirFile));
            }
            System.setProperty("outputDir", directoryName);
        }

        ApplicationContext ctx = createApplicationContext();

		CamelContext camelContext = ctx.getBean("cmdline", CamelContext.class);
		ProducerTemplate camel = camelContext.createProducerTemplate();

		Map<String, Object> headers = new HashMap<String, Object>();

        if (originalFileNames) {
            headers.put(ShsCmdlineHeaders.USE_ORIGINAL_FILENAMES, originalFileNames);
        }

        if (shsTxId != null) {
            headers.put(ShsHeaders.TXID, shsTxId);
        }

        headers.put(ShsCmdlineHeaders.TO_URN, ShsUrn.valueOf(shsTo).toString());

        addQueryParameters(headers);

		try {

			execute(camel, headers);

		} catch (CamelExecutionException e) {
			ShsCmdlineExceptionHandler.handleException(e);
		}
	}

    private void
    addQueryParameters(Map<String, Object> headers) {
        Map<String, Object> queryParams = new HashMap<String, Object>();

        if (noAck) {
            queryParams.put("filter", "noack");
        }

        if (onlyTestMessages) {
            queryParams.put("status", "test");
        }

        if (since != null) {
            queryParams.put("since", since.toString("yyyy-MM-dd'T'HH:mm:ss"));
        }

        for (Map.Entry<String, String> metaEntry : meta.entrySet()) {
            queryParams.put("meta-" + metaEntry.getKey(), metaEntry.getValue());
        }

        if (maxHits != null) {
            queryParams.put("maxhits", String.valueOf(maxHits));
        }

        if (retrieveSingleFile) {
            queryParams.put("maxhits", "1");
        }

        if (shsEndRecipient != null) {
            queryParams.put("endrecipient", shsEndRecipient);
        }

        if (productTypes != null) {
            StringBuilder productTypesString = new StringBuilder();

            for (String productType : productTypes) {
                if (productTypesString.length() != 0) {
                    productTypesString.append(", ");
                }
                productTypesString.append(ShsUrn.valueOf(productType).toUrnForm());
            }
            queryParams.put("producttype", productTypesString.toString());
        }

        if (shsCorrId != null) {
            queryParams.put("corrid", shsCorrId);
        }

        if (sequenceType != null) {
            queryParams.put("sequencetype", sequenceType);
        }

        headers.put(ShsCmdlineHeaders.QUERY_PARAMS, queryParams);
    }

    public void execute(ProducerTemplate camel, final Map<String, Object> headers)
		throws Throwable
	{
        String endpointName = getEndpointName();

        camel.sendBodyAndHeaders(endpointName, null, headers);
    }

    private String getEndpointName() {
        if (listMode) {
            return "direct:listMessages";
        } else if (shsTxId != null) {
            return "direct:fetch";
        } else {
            return "direct:fetchAll";
        }
    }
}
