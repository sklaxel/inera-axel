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
package se.inera.axel.shs.mime;

import javax.activation.DataHandler;


public class DataPart {

	String dataPartType;
	String fileName;
	String contentType;
	Long contentLength;
	TransferEncoding transferEncoding;
	DataHandler dataHandler;
	
	public DataPart() {
		
	}
	
	public DataPart(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
		this.contentType = dataHandler.getContentType();
		this.fileName = dataHandler.getName();
	}	

	public DataHandler getDataHandler() {
		return dataHandler;
	}	
	
	public void setDataHandler(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}
	
	public TransferEncoding getTransferEncoding() throws Exception {
		return transferEncoding;
	}

	public String getDataPartType() {
		return dataPartType;
	}

	public void setDataPartType(String dataPartType) {
		this.dataPartType = dataPartType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getContentLength() {
		return contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	public void setTransferEncoding(TransferEncoding transferEncoding) {
		this.transferEncoding = transferEncoding;
	}


	@Override
	public String toString() {
		return "DataPart{" +
				"dataPartType='" + dataPartType + '\'' +
				", fileName='" + fileName + '\'' +
				", contentType='" + contentType + '\'' +
				", contentLength=" + contentLength +
				", transferEncoding=" + transferEncoding +
				", dataHandler=" + dataHandler +
				'}';
	}
}
