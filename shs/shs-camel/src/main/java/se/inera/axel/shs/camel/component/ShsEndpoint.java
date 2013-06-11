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
package se.inera.axel.shs.camel.component;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 * Represents a HelloWorld endpoint.
 */
public class ShsEndpoint extends DefaultEndpoint {
	private ShsExceptionHandler exceptionHandler;
	private String destinationUri;

    public ShsEndpoint() {
    }

    public ShsEndpoint(String uri, ShsComponent component) {
        super(uri, component);
    }

    public ShsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new ShsProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new ShsConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }

	public ShsExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(ShsExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public String getDestinationUri() {
		return destinationUri;
	}

	public void setDestinationUri(String uri) {
		this.destinationUri = uri;
	}
}
