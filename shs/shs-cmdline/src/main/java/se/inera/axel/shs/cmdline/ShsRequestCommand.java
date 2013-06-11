/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.IOUtils;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.TransferType;

import java.io.*;
import java.util.Map;


@Parameters(commandDescription = "Sends a synchronous request to an SHS Server and  writes the response to a file")
public class ShsRequestCommand extends ShsSendCommand {

	@Parameter(names = {"-out", "--outFile"}, description = "File name where response is written. Defaults to stdout")
	public String outFileName;


	@Override
	public void execute(ProducerTemplate camel, Map<String, Object> headers, InputStream inputStream)
		throws Throwable
	{

		OutputStream responseStream = null;
		try {
			if (outFileName == null || outFileName.equals("-")) {
				responseStream = System.out;
			} else {
				File outFile = new File(outFileName);
				responseStream = new BufferedOutputStream(new FileOutputStream(outFile));
			}

			headers.put(ShsHeaders.TRANSFERTYPE, TransferType.SYNCH);
			headers.put(ShsHeaders.SEQUENCETYPE, sequenceType);

			InputStream result = camel.requestBodyAndHeaders("direct:shsSendSync", inputStream, headers, InputStream.class);
			if (result == null)
				throw new RuntimeException("No response received");

			IOUtils.copy(result, responseStream);
			System.out.println("");
		} finally {
			IOUtils.closeQuietly(responseStream);
		}
	}
}