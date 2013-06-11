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
package se.inera.axel.shs.processor;


import javax.activation.DataSource;
import javax.mail.internet.SharedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamDataSource implements DataSource {
	private SharedInputStream inputStream;
	String contentType = "*/*";
	String name = "InputStreamDataSource";

	public InputStreamDataSource(InputStream inputStream) throws IOException {
        this.inputStream = (SharedInputStream) SharedDeferredStream.toSharedInputStream(inputStream);
	}

	public InputStreamDataSource(InputStream inputStream, String contentType) throws IOException {
        this.inputStream = (SharedInputStream) SharedDeferredStream.toSharedInputStream(inputStream);
		this.contentType = contentType;
	}

	public InputStreamDataSource(InputStream inputStream, String contentType, String name) throws IOException {
        this.inputStream = (SharedInputStream) SharedDeferredStream.toSharedInputStream(inputStream);
		this.contentType = contentType;
		this.name = name;
	}


	@Override
	public InputStream getInputStream() throws IOException {
		return inputStream.newStream(0, -1);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getName() {
		return name;
	}
}