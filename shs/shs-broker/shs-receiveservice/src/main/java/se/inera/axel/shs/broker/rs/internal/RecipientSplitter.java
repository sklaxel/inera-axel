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
package se.inera.axel.shs.broker.rs.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.camel.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;


/**
 * @author Ekkehart Lötzsch
 *
 */
public class RecipientSplitter {
	private static final Logger log = LoggerFactory.getLogger(RecipientSplitter.class);
	 
    @Autowired
    MessageLogService messageLogService;

    /**
     * The split message method returns something that is iteratable such as a java.util.List.
     *
     * @param header the header of the incoming message with the name user
     * @param body the payload of the incoming message
     * @return a list containing each part splitted
     */
    
    /**
     * Clones the received SHS message entry for each recipient in the receiver list and returns a
     * list of the cloned SHS messages.
     * @param receivedShsMessageEntry The SHS message entry.
     * @param shsReceiverList The receiver list which is a list of organization numbers.
     * @return The list of cloned SHS messages.
     */
    public List<ShsMessage> split(ShsMessageEntry receivedShsMessageEntry, @Property(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST) List<String> shsReceiverList) {

    	List<ShsMessage> answer = new ArrayList<ShsMessage>();
    	if (shsReceiverList == null) {
    		log.error("RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST should never be null");
    	} else {
            ShsMessage receivedShsMessage = messageLogService.loadMessage(receivedShsMessageEntry);

        	for (String organizationNumber : shsReceiverList) {
        		
        		// Clone the ShsLabel
                ShsLabel clonedLabel = ShsLabel.newInstance(receivedShsMessageEntry.getLabel());
                
                // Set new txId
                clonedLabel.setTxId(UUID.randomUUID().toString());

            	// Clear label history
                clonedLabel.getHistory().clear();
            	
        		// Set To
        		To to = clonedLabel.getTo();
        		if (to == null) {
        			to = new To();
        			to.setValue(organizationNumber);
        			clonedLabel.setTo(to);
        		} else {
    				to.setValue(organizationNumber);
    				to.setCommonName(null);
        		}

            	// Create ShsMessage with the cloned ShsLabel
                ShsMessage clonedShsMessage = new ShsMessage();
                clonedShsMessage.setLabel(clonedLabel);
                
                // EKLO TODO - Add data parts
                // - is it OK if I just copy the ArrayList as a shallow copy?
                
                // Add data parts
                clonedShsMessage.setDataParts(receivedShsMessage.getDataParts());

                answer.add(clonedShsMessage);
        	}
     	}
   	
    	return answer;
    }
}