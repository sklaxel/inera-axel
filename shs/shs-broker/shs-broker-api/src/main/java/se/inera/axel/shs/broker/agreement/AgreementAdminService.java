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
package se.inera.axel.shs.broker.agreement;

import java.util.List;

import se.inera.axel.shs.xml.agreement.ShsAgreement;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface AgreementAdminService {
    @GET
    @Path("/{agreementId}")
	ShsAgreement findOne(@PathParam("agreementId") String agreementId);
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	List<ShsAgreement> findAll();
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public void save(ShsAgreement agreement);
    public void delete(ShsAgreement agreement);
    @DELETE
    @Path("/{agreementId}")
    public void delete(@PathParam("agreementId") String agreementId);
    
}
