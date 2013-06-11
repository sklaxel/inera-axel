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
package se.inera.axel.shs.exception;

/**
 * @author Jan Hallonstén, R2M
 *
 */
public class MissingDeliveryExecutionException extends ShsException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String ERROR_CODE = "MissingDeliveryExecution";
	
	private static final String ERROR_INFO = "Delivery of message failed";

	public MissingDeliveryExecutionException() {
		super(ERROR_CODE, ERROR_INFO);
	}
	
	public MissingDeliveryExecutionException(String errorInfo) {
		super(ERROR_CODE, errorInfo);
	}
	
	public MissingDeliveryExecutionException(Throwable cause) {
		super(ERROR_CODE, ERROR_INFO, cause);
	}
	
	public MissingDeliveryExecutionException(String errorInfo, Throwable cause) {
		super(ERROR_CODE, errorInfo, cause);
	}
}
