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

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Represents the component that manages {@link ShsEndpoint}.
 */
public class ShsComponent extends DefaultComponent {

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        ShsEndpoint endpoint = new ShsEndpoint(uri, this);
        
        ShsExceptionHandler exceptionHandler = getAndRemoveParameter(parameters, "exceptionHandler", ShsExceptionHandler.class);
        if (exceptionHandler == null) {
        	exceptionHandler = new DefaultShsExceptionHandler();
        }
        setProperties(exceptionHandler, parameters);
        
        endpoint.setExceptionHandler(exceptionHandler);
        
        setProperties(endpoint, parameters);
        
        // TODO add remaining parameters to URI
        endpoint.setDestinationUri(remaining);
        
        return endpoint;
    }
}
