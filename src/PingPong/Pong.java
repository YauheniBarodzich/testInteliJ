package PingPong;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;

public class Pong extends Agent {
    private int i = 0;

    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    i++;
//                    if (i > 3) {
//                        doDelete();
//                    }
                    System.out.println("POng: content = " + msg.getContent());
                    msg.createReply();
                    msg.setContent("reply epta!");
                    msg.setPerformative(ACLMessage.INFORM);
                    AID sender = msg.getSender();
                    System.out.println("POng: sender = " + sender);
                    msg.clearAllReceiver();
                    msg.addReceiver(sender);
                    msg.addReceiver(getAID("PIngAgent"));
                    send(msg);
                }
            }
        });
    }
}
