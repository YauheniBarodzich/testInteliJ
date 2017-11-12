package INZ.CNP.Ontology;

import jade.content.AgentAction;
import jade.content.Predicate;

/**
 * Created by Mars on 12.11.2017.
 */
public class IsBusy implements Predicate {
    private String conversationID;

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }
}
