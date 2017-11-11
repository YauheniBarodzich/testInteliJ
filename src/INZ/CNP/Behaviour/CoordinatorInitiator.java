package INZ.CNP.Behaviour;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Created by Mars on 30.10.2017.
 */
public class CoordinatorInitiator extends ContractNetInitiator {

    public CoordinatorInitiator(Agent myAgent, ACLMessage message){
        super(myAgent, message);
        System.out.println("CO const");
    }


}
