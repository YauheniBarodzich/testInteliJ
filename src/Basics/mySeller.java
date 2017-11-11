package Basics;

import INZ.Agent.Behaviour.DelayBehaviour;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.lang.acl.ACLMessage;

import java.util.Random;

/**
 * Created by Mars on 18.09.2017.
 */
public class mySeller extends Agent{

    Random  rnd = new Random();
    MessageTemplate queryRef = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
    MessageTemplate RequestTemplateMessage = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {

            @Override
            public void action() {

                ACLMessage msg = receive(queryRef);
                if (msg != null){
                    addBehaviour(new Transaction(myAgent,msg));
                }
                block();
            }
        });
    }

    class Transaction extends SequentialBehaviour{

        ACLMessage msg, reply;
        String ConvID;

        int    price  = rnd.nextInt(100);

        public Transaction(Agent a, ACLMessage msg){
            super(a);
            this.msg = msg;
            ConvID = msg.getConversationId();
        }

        @Override
        public void onStart() {

            addSubBehaviour( new DelayBehaviour(myAgent,2000){

                @Override
                protected void handleElapsedTimeout() {
                    reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("" + price);
                    System.out.println(myAgent.getLocalName() + " send INFORM " + reply.getContent());
                    send(reply);
                }
            });

            addSubBehaviour( new myReceiver(myAgent,1000,RequestTemplateMessage){
                @Override
                public void handle(ACLMessage msg1) {
                    super.handle(msg1);

                    if(msg1 != null)
                    {
                        System.out.println("Receive REQUEST message" + msg1.getPerformative() );
                        reply = msg1.createReply();
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("" + price);
                        send(reply);
                    }
                }
            });



        }
    }
}
