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
package se.inera.axel.shs.camel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageTestObjectMother;


public class AbstractShsTestNGTests extends AbstractTestNGSpringContextTests {
	
	@Produce(context="camel-test")
    ProducerTemplate template;
	
	@EndpointInject(uri = "mock:testResult")
    MockEndpoint resultEndpoint;

	public ShsMessage createTestMessage() {
		return ShsMessageTestObjectMother.createTestMessage();
	}

	/**
	 * Compare two input stream
	 * 
	 * @param input1 the first stream
	 * @param input2 the second stream
	 * @return true if the streams contain the same content, or false otherwise
	 * @throws IOException
	 * @throws IllegalArgumentException if the stream is null
	 */
	public static boolean isSame( InputStream input1,
			InputStream input2 ) throws IOException {
		boolean error = false;
		try {
			byte[] buffer1 = new byte[1024];
			byte[] buffer2 = new byte[1024];
			try {
				int numRead1 = 0;
				int numRead2 = 0;
				while (true) {
					numRead1 = input1.read(buffer1);
					numRead2 = input2.read(buffer2);
					if (numRead1 > -1) {
						if (numRead2 != numRead1) return false;
						// Otherwise same number of bytes read
						if (!Arrays.equals(buffer1, buffer2)) return false;
						// Otherwise same bytes read, so continue ...
					} else {
						// Nothing more in stream 1 ...
						return numRead2 < 0;
					}
				}
			} finally {
				input1.close();
			}
		} catch (IOException e) {
			error = true; // this error should be thrown, even if there is an error closing stream 2
			throw e;
		} catch (RuntimeException e) {
			error = true; // this error should be thrown, even if there is an error closing stream 2
			throw e;
		} finally {
			try {
				input2.close();
			} catch (IOException e) {
				if (!error) throw e;
			}
		}
	}
	
}
