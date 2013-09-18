package se.inera.axel.shs.broker.directory.internal.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class NotFoundException extends WebApplicationException {
    public NotFoundException() {
        super(Response.status(Response.Status.NOT_FOUND).build());
    }
}
