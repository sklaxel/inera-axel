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
package se.inera.axel.shs.camel;

import org.apache.commons.httpclient.methods.RequestEntity;
import se.inera.axel.shs.mime.ShsMessage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A RequestEntity that represents an ShsMessage.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsMessageRequestEntity implements RequestEntity {
    private final ShsMessage shsMessage;

    public ShsMessageRequestEntity(ShsMessage message) {
        this.shsMessage = message;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void writeRequest(OutputStream out) throws IOException {
        ShsMessageTypeConverter.marshaller.marshal(shsMessage, out);
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public String getContentType() {
        return "multipart/mixed";
    }
}
