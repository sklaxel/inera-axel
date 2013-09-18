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

import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.ArrayList;
import java.util.List;

public class ShsMessage {
	
	private ShsLabel label;
	private List<DataPart> dataParts = new ArrayList<DataPart>();
	
	public ShsLabel getLabel() {
		return label;
	}
	
	public void setLabel(ShsLabel label) {
		this.label = label;
	}
	
	public List<DataPart> getDataParts() {
		return dataParts;
	}
	
	public void addDataPart(DataPart part) {
		dataParts.add(part);
	}
	
	@Override
	public String toString() {

		String txId = label == null ? null : label.getTxId();

		return "ShsMessage{" +
				"txId=" + txId +
				", dataParts=" + dataParts +
				'}';
	}

	public void setDataParts(List<DataPart> dataParts) {
		this.dataParts = dataParts;
	}
}
