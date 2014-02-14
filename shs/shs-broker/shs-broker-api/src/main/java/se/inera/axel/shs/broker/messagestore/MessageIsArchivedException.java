/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 */

package se.inera.axel.shs.broker.messagestore;

public class MessageIsArchivedException extends RuntimeException{
	
	public MessageIsArchivedException() {
		
	}
	
	public MessageIsArchivedException(String message) {
		super(message);
	}
}
