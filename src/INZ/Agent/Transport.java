package INZ.Agent;

import INZ.Agent.Behaviour.DelayBehaviour;
import INZ.Agent.Behaviour.Receiver;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.Random;

/**
 * Created by Mars on 21.10.2017.
 */
public class Transport extends Agent {

    public int testVar;

    boolean hasOrder = false;

    Random randomNumber = new Random();
    MessageTemplate QueryReferenceTemplate = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
    MessageTemplate RequestTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage message = receive(QueryReferenceTemplate);
                if (message != null) {
                    System.out.println(myAgent.getLocalName() + " get performative" + message.getPerformative() + "from agent: !" + message.getSender().getLocalName());
                    addBehaviour(new Transaction(myAgent, message));
                }
                block();
            }
        });
    }

    class Transaction extends SequentialBehaviour {
        ACLMessage message, reply;
        String ConversationID;

        private int randomPrice = randomNumber.nextInt(100);

        public Transaction(Agent myAgent, ACLMessage message) {
            super(myAgent);
            this.message = message;
            this.ConversationID = message.getConversationId();
        }

        @Override
        public void onStart() {
            super.onStart();


            TransportInformDelayBehaviour transportInformDelayBehaviour = new TransportInformDelayBehaviour(myAgent, 1500);
            addSubBehaviour(transportInformDelayBehaviour);

            //            addSubBehaviour(new DelayBehaviour(myAgent, 1500) {
//                protected void handleElapsedTimeout() {
//                    reply = message.createReply();
//                    reply.setPerformative(ACLMessage.INFORM);
//                    reply.setContent("" + randomPrice);
////                    reply.setConversationId(ConversationID);
//                    System.out.println(myAgent.getLocalName() + " send message id = " + reply.getConversationId() + " content: " + reply.getContent());
//                    send(reply);
//                }
//            });

            RequestTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId(ConversationID));
            TransportReceiver receiver = new TransportReceiver(myAgent, 8000, RequestTemplate);

            addSubBehaviour(receiver);
//            addSubBehaviour(new Receiver(myAgent, 5000, RequestTemplate) {
//                @Override
//                public void handle(ACLMessage message) {
//                    if (message != null) {
//                        System.out.println("GET REQUEST -> AGREE/REFUSE " + myAgent.getLocalName());
//
//                        hasOrder = true;
//                        reply = message.createReply();
//                        if (randomPrice > 5) {
//                            reply.setPerformative(ACLMessage.AGREE);
//                            send(reply);
//                        } else {
//                            System.out.println("Refuse and Delete from " + myAgent.getLocalName());
//                            reply.setPerformative(ACLMessage.REFUSE);
//                            send(reply);
//                            myAgent.doDelete();
//
////                            myAgent.takeDown();
//                        }
//                    } else {
//                        System.out.println(myAgent.getLocalName() + ": do Delete()");
//                        this.done();
////                        myAgent.doDelete();
//                    }
//                }
//            });

            TransportDoneDelayBehaviour transportDoneDelayBehaviour = new TransportDoneDelayBehaviour(myAgent, 7000);


            addSubBehaviour(transportDoneDelayBehaviour);
//            addSubBehaviour(new DelayBehaviour(myAgent, 7000) {
//                public void handleElapsedTimeout() {
//                    reply = message.createReply();
//                    reply.setPerformative(ACLMessage.INFORM);
//                    reply.setContent("done");
//                    System.out.println(myAgent.getLocalName() + " send done");
//                    send(reply);
//                }
//            });

        }

        class TransportInformDelayBehaviour extends DelayBehaviour {
            public TransportInformDelayBehaviour(Agent a, long timeout) {
                super(a, timeout);
            }

            protected void handleElapsedTimeout() {
                reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("" + randomPrice);
                System.out.println(myAgent.getLocalName() + " send message id = " + reply.getConversationId() + " content: " + reply.getContent());
                send(reply);
            }
        }

        class TransportReceiver extends Receiver {
            public TransportReceiver(Agent a, int millis, MessageTemplate mt) {
                super(a, millis, mt);
            }

            public void handle(ACLMessage message) {
                System.out.println("TransportReceiver");

                if (message != null) {
                    System.out.println(myAgent.getLocalName() + ": GET REQUEST -> AGREE/REFUSE ");

                    reply = message.createReply();
                    if (randomPrice > 40) {
                        reply.setPerformative(ACLMessage.AGREE);
                        hasOrder = true;
                        send(reply);
                    } else {
                        System.out.println(myAgent.getLocalName() + ": REFUSE. Agetn will delete." );
                        reply.setPerformative(ACLMessage.REFUSE);
                        send(reply);
//                        this.reset();
                        //                            myAgent.takeDown();
                    }
                } else {
                    System.out.println(myAgent.getLocalName() + ": do Delete()");
//                    this.reset();
//                        myAgent.doDelete();
                }
            }
        }

        class TransportDoneDelayBehaviour extends DelayBehaviour {
            public TransportDoneDelayBehaviour(Agent a, long timeout) {
                super(a, timeout);
            }

            public void handleElapsedTimeout() {
                System.out.println("TransportDoneDelayBehaviour");
                if (hasOrder){
                reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("done");
                System.out.println(myAgent.getLocalName() + " send DONE");
                send(reply);
                }
            }
        }

        @Override
        public int onEnd() {
            System.out.println(myAgent.getLocalName() + " bye-bye!");
            return super.onEnd();
        }
    }


}


