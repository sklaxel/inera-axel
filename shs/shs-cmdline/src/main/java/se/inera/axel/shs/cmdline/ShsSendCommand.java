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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.mime.TransferEncoding;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.TransferType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Sends an asynchronous message to an SHS Server")
public class ShsSendCommand {

	@Parameter(names = {"-o", "--originator"}, description = "Originator (Sender)")
	public String shsOriginator;
	@Parameter(names = {"-e", "--endRecipient"}, description = "End recipient")
	public String shsEndRecipient;
	@Parameter(names = {"-f", "--from"}, description = "From address (OrgNbr)")
	public String shsFrom;
	@Parameter(names = {"-t", "--to"}, description = "To address (OrgNbr)")
	public String shsTo;
	@Parameter(names = {"-s", "--subject"}, description = "Subject of message")
	public String shsSubject;
	@Parameter(names = {"--corrId"}, description = "Correlation Id (Defaults to TxId of a new message)")
	public String shsCorrId;
	@Parameter(names = {"-c", "--contentId"}, description = "Content id of the message")
	public String shsContentId;
	@Parameter(names = {"--comment"}, description = "Content comment")
	public String shsContentComment;
	@Parameter(names = {"-p", "--product"}, description = "Product (UUID)")
	public String shsProductId = "00000000-0000-0000-0000-000000000000";
	@Parameter(names = "--status")
	public Status status = Status.PRODUCTION;
	@Parameter(names = "--sequenceType")
	public SequenceType sequenceType = SequenceType.REQUEST;
	@Parameter(names = "--dataPartType", description = "Defaults to filename extension of the input file")
	public String dataPartType = null;
	@Parameter(names = "--contentType", description = "Content type of the input file")
	public String contentType = "application/octet-stream";
	@Parameter(names = "--transferEncoding", description = "DataPart transfer encoding")
	public TransferEncoding transferEncoding = TransferEncoding.BASE64;
	@Parameter(names = "--fileName", description = "DataPart file name. Defaults to name of input file. Must be specified when using standard input.")
	public String fileName;
	@Parameter(names = "--contentLength", description = "DataPart content length. Defaults to size of input file. May be specified when using standard input.")
	public Long contentLength;
	@Parameter(names = { "-in", "--inFile" }, description = "Input file. Defaults to stdin")
	public String inFileName;
	@DynamicParameter(names = {"-m", "--meta"}, description = "Meta data. The meta parameter is allowed multiple times.")
	public Map<String, String> meta = new HashMap<String, String>();


	public final void execute() throws Throwable {
		String contextFile = System.getProperty("spring-context-location", "classpath:shs-cmdline-context.xml");

		ApplicationContext ctx = new ClassPathXmlApplicationContext(contextFile);

		CamelContext camelContext = ctx.getBean("cmdline", CamelContext.class);
		ProducerTemplate camel = camelContext.createProducerTemplate();

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(ShsHeaders.FROM, shsFrom);
		headers.put(ShsHeaders.ORIGINATOR, shsOriginator);
		headers.put(ShsHeaders.ENDRECIPIENT, shsEndRecipient);
		headers.put(ShsHeaders.TO, shsTo);
		headers.put(ShsHeaders.SUBJECT, shsSubject);
		headers.put(ShsHeaders.CONTENT_ID, shsContentId);
		headers.put(ShsHeaders.CONTENT_COMMENT, shsContentComment);
		headers.put(ShsHeaders.CORRID, shsCorrId);

		headers.put(ShsHeaders.PRODUCT_ID, shsProductId);
		headers.put(ShsHeaders.SEQUENCETYPE, sequenceType);

		if (transferEncoding != null)
			headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, transferEncoding);

		if (contentType != null)
			headers.put(ShsHeaders.DATAPART_CONTENTTYPE, contentType);

		if (sequenceType != null)
			headers.put(ShsHeaders.SEQUENCETYPE, sequenceType);

		if (status != null)
			headers.put(ShsHeaders.STATUS, status);

		if (meta != null && !meta.isEmpty()) {
			headers.put(ShsHeaders.META, meta);
		}

		InputStream inputStream;
		if (inFileName == null || inFileName.isEmpty() || inFileName.equals("-")) {
			inputStream = System.in;

			if (StringUtils.isEmpty(fileName))
				throw new RuntimeException("The '--fileName' option must be used when reading from stdin");

			headers.put(ShsHeaders.DATAPART_FILENAME, fileName);
			headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, contentLength);

		} else {
			File inFile = new File(inFileName);

			if (!inFile.exists()) {
				throw new RuntimeException("File not found: " + inFile);
			}

			if (!inFile.canRead()) {
				throw new RuntimeException("Can't read file: " + inFile);
			}

			if (fileName != null)
				headers.put(ShsHeaders.DATAPART_FILENAME, fileName);
			else
				headers.put(ShsHeaders.DATAPART_FILENAME, inFile.getName());

			if (contentLength != null)
				headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, contentLength);
			else
				headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, inFile.length());


			inputStream = new BufferedInputStream(new FileInputStream(inFile));
		}

		if (dataPartType == null) {
			dataPartType = FilenameUtils.getExtension((String) headers.get(ShsHeaders.DATAPART_FILENAME));
		}

		if (StringUtils.isEmpty(dataPartType)) {
			throw new RuntimeException("Datapart not specified and no file extension found");
		}

		headers.put(ShsHeaders.DATAPART_TYPE, dataPartType);

		try {

			execute(camel, headers, inputStream);

		} catch (CamelExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof HttpOperationFailedException) {
				HttpOperationFailedException httpException = (HttpOperationFailedException)cause;

				ShsHttpException shsHttpException =
						new ShsHttpException(httpException.getResponseBody(),
								httpException.getStatusText(),
								httpException.getResponseHeaders(),
								httpException.getStatusCode());

				throw shsHttpException;
			}

			throw e.getCause();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public void execute(ProducerTemplate camel, Map<String, Object> headers, InputStream inputStream)
		throws Throwable
	{
		headers.put(ShsHeaders.TRANSFERTYPE, TransferType.ASYNCH);
		String txId = camel.requestBodyAndHeaders("direct:shsSendAsync", inputStream, headers, String.class);

		System.out.println(txId);
	}

}