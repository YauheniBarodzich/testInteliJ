package Basics;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.lang.acl.ACLMessage;

/**
 * Created by Mars on 01.10.2017.
 */
public class myBuyer extends Agent {

    ACLMessage msg, reply;

    MessageTemplate InformTemplateMessage = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    MessageTemplate AgreeTemplateMessage = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
    MessageTemplate RefuseTemplateMessage = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);

    MessageTemplate AgreeOrRefuseMessage = MessageTemplate.or(AgreeTemplateMessage, RefuseTemplateMessage);

    @Override
    protected void setup() {
        super.setup();

        SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
        addBehaviour(sequentialBehaviour);

        //ParallelBehaviour parallelBehaviour = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

        sequentialBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = new ACLMessage();
//                msg.setContent("Hello");
                msg.setPerformative(ACLMessage.QUERY_REF);
                msg.addReceiver(new AID("Helen", AID.ISLOCALNAME));
                send(msg);
            }
        });

        sequentialBehaviour.addSubBehaviour(new myReceiver(this, 3000, InformTemplateMessage) {
            @Override
            public void handle(ACLMessage msg) {

                if (msg != null) {
                    if (Integer.parseInt(msg.getContent()) < 700) {
                        System.out.println("Byer: I will take!");
                        reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REQUEST);
                        send(reply);
                    } else {
                        System.out.println("Don't want this sheet");
                    }
                }
            }
        });

        sequentialBehaviour.addSubBehaviour(new myReceiver(this, 10000, AgreeOrRefuseMessage) {
            @Override
            public void handle(ACLMessage msg) {

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.AGREE) {
                        System.out.println("FINISHED");
//                        setup();
                    } else {
                        // WHYYYYYYYY IS IT FINISHED BY SETUP ?????
//                        setup();
                    }
                }
            }
        });

    }

    @Override
    protected void takeDown() {
        super.takeDown();
        System.out.println("Buyer Agent is terminate!");
    }
}
