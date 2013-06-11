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

import java.util.SortedMap;
import java.util.TreeMap;



public class ShsException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean fatal = true;

	private SortedMap<String, String> appInfo = new TreeMap<String, String>();
	private String contentId = null;
	private String corrId = null;
	private String errorCode = null;
	private String errorInfo = null;
	
	public SortedMap<String, String> getAppInfo() {
		return appInfo;
	}

	public void appendAppInfo(String name, String value) {
		this.appInfo.put(name, value);
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getCorrId() {
		return corrId;
	}

	public void setCorrId(String corrId) {
		this.corrId = corrId;
	}
	 
	protected ShsException(String errorCode, String errorInfo) {
		this.errorCode = errorCode;
		setErrorInfo(errorInfo);
	}

	protected ShsException(String errorCode, String errorInfo, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
		setErrorInfo(errorInfo);
	}

	protected ShsException(String errorCode) {
		this.errorCode = errorCode;
		setErrorInfo(null);
	}

	protected ShsException(String errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
		setErrorInfo(null);
	}

	private void setErrorInfo(String errorInfo) {		
		this.errorInfo = errorInfo;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorInfo() {
        if (errorInfo != null) {
            return errorInfo;
        } else {
        	return "";
        }
	}

	@Override
	public String getMessage() {
		return toString();
	}

	protected String getCauseMessage() {
		StringBuilder message = new StringBuilder();
		
		if (getCause() != null) {
			message.append(getCause().getClass().getSimpleName())
			.append(" [message=")
			.append(getCause().getMessage())
			.append("]");
            
		}
		return message.toString();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [contentId=" + contentId + ", corrId=" + corrId
				+ ", errorCode=" + errorCode + ", errorInfo=" + errorInfo + ", cause=" + getCauseMessage() + "]";
	}
	
}
