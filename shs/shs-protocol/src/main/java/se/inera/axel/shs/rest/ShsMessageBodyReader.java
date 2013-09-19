package se.inera.axel.shs.rest;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Consumes("message/rfc822")
@Provider
public class ShsMessageBodyReader implements MessageBodyReader<ShsMessage> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public ShsMessage readFrom(Class<ShsMessage> shsMessageClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> stringStringMultivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {

        ShsMessageMarshaller marshaller = new ShsMessageMarshaller();

        try {
            return marshaller.unmarshal(inputStream);
        } catch (Exception e) {
            throw new IOException("Error unmarshalling shs message", e);
        }

    }

}
