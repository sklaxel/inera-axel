package se.inera.axel.shs.rest;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Produces("message/rfc822")
@Provider
public class ShsMessageBodyWriter implements MessageBodyWriter<ShsMessage> {

    public long getSize(ShsMessage message, Class<?> type, Type genericType, Annotation[] annotations, MediaType mt) {
        return -1;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mt) {
        return ShsMessage.class.isAssignableFrom(type);
    }

    public void writeTo(ShsMessage message, Class<?> clazz, Type type, Annotation[] a,
                        MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os)
            throws IOException {

        ShsMessageMarshaller marshaller = new ShsMessageMarshaller();

        try {
            marshaller.marshal(message, os);
        } catch (Exception e) {
            throw new IOException("Error marshalling shs message", e);
        }

    }
}
