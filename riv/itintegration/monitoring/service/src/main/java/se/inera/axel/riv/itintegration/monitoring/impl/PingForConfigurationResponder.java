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
package se.inera.axel.riv.itintegration.monitoring.impl;

import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

import javax.jws.WebParam;
import javax.jws.WebService;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebService(endpointInterface = "se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface")
public class PingForConfigurationResponder implements PingForConfigurationResponderInterface {

    @Override
    public PingForConfigurationResponseType pingForConfiguration(
            @WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true)
            String logicalAddress,
            @WebParam(partName = "parameters", name = "PingForConfiguration", targetNamespace = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1")
            PingForConfigurationType parameters) {
        if (logicalAddress == null) {
            throw new RuntimeException("Testing exception handling: No ws-addressing 'To'-address found in message");
        }

        if ("1111111111".equalsIgnoreCase(logicalAddress)) {
            throw new RuntimeException("Testing exception handling: illegal 'To'-address: " + logicalAddress);
        }

        if (parameters == null || parameters.getServiceContractNamespace() == null) {
            throw new RuntimeException("Testing soap fault. No service namespace specified.");
        }

        PingForConfigurationResponseType response = new PingForConfigurationResponseType();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        response.setVersion("1.0");
        response.setPingDateTime(format.format(new Date()));

        return response;
    }
}

