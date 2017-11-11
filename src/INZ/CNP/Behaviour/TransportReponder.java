package INZ.CNP.Behaviour;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.Random;

/**
 * Created by Mars on 30.10.2017.
 */
public class TransportReponder extends ContractNetResponder{

    Random randomNumber = new Random();
    public int randomPrice = randomNumber.nextInt(100);

    public TransportReponder(Agent myAgent, MessageTemplate template){
        super(myAgent, template);
    }

}
