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
package se.inera.axel.shs.cmdline;


import java.util.Map;

public class ShsHttpException extends RuntimeException {
	String responseBody;
	String statusText;
	Map<String, String> responseHeaders;
	int statusCode;

	public ShsHttpException(String responseBody, String statusText, Map<String, String> responseHeaders, int statusCode) {
		this.responseBody = responseBody;
		this.statusText = statusText;
		this.responseHeaders = responseHeaders;
		this.statusCode = statusCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public String getStatusText() {
		return statusText;
	}

	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String toString() {
		return "ShsHttpException{" +
				"responseBody='" + responseBody + '\'' +
				", statusText='" + statusText + '\'' +
				", responseHeaders=" + responseHeaders +
				", statusCode=" + statusCode +
				'}';
	}
}
