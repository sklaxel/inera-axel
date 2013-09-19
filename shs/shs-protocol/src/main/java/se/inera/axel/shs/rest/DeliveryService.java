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
package se.inera.axel.shs.rest;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.message.ShsMessageList;

import javax.ws.rs.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 * @author Björn Bength, bjorn.bength@r2m.se
 */
@Path("{orgId}")
public interface DeliveryService {

    @GET
    @Produces("application/xml")
    ShsMessageList listMessages(@PathParam("orgId") String orgId);

    @GET
    @Produces("message/rfc822")
    @Path("{txId}")
    ShsMessage fetchMessage(@PathParam("orgId") String orgId, @PathParam("txId") String txId);

    @POST
    @Path("{txId}?action=ack")
    void acknowledgeMessage(@PathParam("orgId") String orgId, @PathParam("txId") String txId);

}