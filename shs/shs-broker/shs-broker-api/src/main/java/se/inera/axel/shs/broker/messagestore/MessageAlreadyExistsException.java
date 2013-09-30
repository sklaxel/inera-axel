package se.inera.axel.shs.broker.messagestore;

public class MessageAlreadyExistsException extends RuntimeException {

    public MessageAlreadyExistsException(String txId) {
        super(txId);
    }

    public MessageAlreadyExistsException(String txId, Throwable cause) {
        super(txId, cause);
    }
}
