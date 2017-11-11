package PingPong;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by Mars on 16.08.2017.
 */
public class Ping extends Agent {
    protected void setup() {
        super.setup();
        addBehaviour(new OneShotBehaviour() {
                         @Override
                         public void action() {
                             ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                             msg.setContent("Hejka~");
                             System.out.println(getAID("POngAgent"));
                             msg.addReceiver(getAID("POngAgent"));
                             send(msg);
                         }
                     }

        );
        addBehaviour(new CyclicBehaviour(

        ) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("Ping: get reply content = " + msg.getContent());
                }
            }
        });
    }
}
