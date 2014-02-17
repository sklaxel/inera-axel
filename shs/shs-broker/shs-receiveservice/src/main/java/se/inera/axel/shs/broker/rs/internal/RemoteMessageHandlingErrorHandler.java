package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.ExchangeException;
import org.apache.camel.Header;
import org.apache.camel.component.http.HttpOperationFailedException;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.processor.ShsHeaders;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RemoteMessageHandlingErrorHandler {
    private MessageLogService messageLogService;

    public RemoteMessageHandlingErrorHandler(MessageLogService messageLogService) {
        this.messageLogService = messageLogService;
    }

    public void handleError(
            ShsMessageEntry shsMessageEntry,
            @Header(ShsHeaders.X_SHS_ERRORCODE) String errorCode,
            @ExchangeException HttpOperationFailedException exception) {
        MissingDeliveryExecutionException e = new MissingDeliveryExecutionException(
                String.format("Delivery of message failed. Remote message handling error: errorCode %s errorInfo %s",
                        errorCode,
                        exception.getResponseBody()),
                exception);

        messageLogService.messageQuarantined(shsMessageEntry, e);
        throw e;
    }
}
