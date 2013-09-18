package se.inera.axel.shs.broker.rs.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.camel.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.To;

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
    public List<ShsMessage> split(ShsMessageEntry origEntry, @Header(value = "user") String header, @Body String body, @Property(RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST) List<String> toOrganisationNumbers) {

    	List<ShsMessage> answer = new ArrayList<ShsMessage>();
    	if (toOrganisationNumbers == null) {
    		log.error("RecipientLabelTransformer.PROPERTY_SHS_RECEIVER_LIST should never be null");
    	} else {
            ShsMessage origShsMessage = messageLogService.fetchMessage(origEntry);

        	for (String orgNbr : toOrganisationNumbers) {
        		
        		// Clone the ShsLabel
                ShsLabel clonedLabel = ShsLabel.newInstance(origEntry.getLabel());
                
                // Set new txId
                clonedLabel.setTxId(UUID.randomUUID().toString());

            	// Clear label history
                clonedLabel.getHistory().clear();
            	
        		// Set To
        		To to = clonedLabel.getTo();
        		if (to == null) {
        			to = new To();
        			to.setValue(orgNbr);
        			clonedLabel.setTo(to);
        		} else {
    				to.setValue(orgNbr);
    				to.setCommonName(null);
        		}

            	// Create ShsMessage with the cloned ShsLabel
                ShsMessage clonedShsMessage = new ShsMessage();
                clonedShsMessage.setLabel(clonedLabel);
                
                // EKLO TODO - Add data parts
                // - is it OK if I just copy the ArrayList as a shallow copy?
                
                // Add data parts
                clonedShsMessage.setDataParts(origShsMessage.getDataParts());

                answer.add(clonedShsMessage);
        	}
     	}
   	
    	return answer;
    }
}