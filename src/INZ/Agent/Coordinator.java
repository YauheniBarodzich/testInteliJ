package INZ.Agent;

import INZ.Agent.Behaviour.DelayBehaviour;
import INZ.Agent.Behaviour.Receiver;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

/**
 * Created by Mars on 21.10.2017.
 */
public class Coordinator extends Agent {

    private boolean finished = false;

    MessageTemplate InformTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    MessageTemplate AgreeTemplate = MatchPerformative(ACLMessage.AGREE);
    MessageTemplate RefuseTemplate = MatchPerformative(ACLMessage.REFUSE);
    MessageTemplate AgreeOrRefuseTemplate = MessageTemplate.or(AgreeTemplate, RefuseTemplate);
    MessageTemplate InformDoneTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchContent("done"));

    ACLMessage messageFirstGroup;
    ACLMessage messageSecondGroup;
    ACLMessage reply;
    TransactionCoordinator TransactionFirstGroup;
    TransactionCoordinator TransactionSecondGroup;

    RefuseByIDConverstion FirstGroupRefuseBehaviour;
    RefuseByIDConverstion SecondGroupRefuseBehavoiur;


    List<String> FirtsGroupNames = new ArrayList<String>() {{
        add("T_11");
        add("T_12");
        add("T_13");
    }};
    List<String> SecondGroupNames = new ArrayList<String>() {{
        add("T_21");
        add("T_22");
        add("T_23");
    }};

    protected void setup() {
        super.setup();

        {
            addBehaviour(new SimpleBehaviour() {
                @Override
                public void action() {
                    messageFirstGroup = newACLMessageWithConversationID(ACLMessage.QUERY_REF);
                    TransactionFirstGroup = new TransactionCoordinator(myAgent, messageFirstGroup, FirtsGroupNames);
                    FirstGroupRefuseBehaviour = new RefuseByIDConverstion(myAgent, messageFirstGroup.getConversationId());

                    ParallelBehaviour FirstGroupParallel = new ParallelBehaviour(myAgent, 1);
                    FirstGroupParallel.addSubBehaviour(TransactionFirstGroup);
                    FirstGroupParallel.addSubBehaviour(FirstGroupRefuseBehaviour);

                    messageSecondGroup = newACLMessageWithConversationID(ACLMessage.QUERY_REF);
                    TransactionSecondGroup = new TransactionCoordinator(myAgent, messageSecondGroup, SecondGroupNames);
                    SecondGroupRefuseBehavoiur = new RefuseByIDConverstion(myAgent, messageSecondGroup.getConversationId());

                    ParallelBehaviour SecondGroupParallel = new ParallelBehaviour(myAgent, 1);
                    SecondGroupParallel.addSubBehaviour(TransactionSecondGroup);
                    SecondGroupParallel.addSubBehaviour(SecondGroupRefuseBehavoiur);

                    ParallelBehaviour mainParallel = new ParallelBehaviour(myAgent, ParallelBehaviour.WHEN_ALL);
                    mainParallel.addSubBehaviour(FirstGroupParallel);
                    mainParallel.addSubBehaviour(SecondGroupParallel);

                    addBehaviour(mainParallel);


                }

                @Override
                public boolean done() {
                    return finished;
                }

                @Override
                public int onEnd() {

                    return super.onEnd();
                }
            });

//            SequentialBehaviour sequentialBehaviourCoordinator = new SequentialBehaviour(this);
//            addBehaviour(sequentialBehaviourCoordinator);

//            sequentialBehaviourCoordinator.addSubBehaviour(new OneShotBehaviour() {
//
//                public void action() {
//                    ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
//
//                    for (int i = 0; i < 3; i++) {
//                        message.addReceiver(new AID("T_" + i, AID.ISLOCALNAME));
//                    }
//                    send(message);
//                }
//            });

//            ParallelBehaviour parallelBehaviourCoordinator = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
//            for (int i = 0; i < 3; i++) {
//                parallelBehaviourCoordinator.addSubBehaviour(new Receiver(this, 2000, InformTemplate) {
//                    public void handle(ACLMessage message) {
//
//                        if (message != null) {
//                            if (Integer.parseInt(message.getContent()) < 60) {
//                                System.out.println(myAgent.getName() + ": I will take by: " + message.getContent());
//                                reply = message.createReply();
//                                reply.setPerformative(ACLMessage.REQUEST);
//                                send(reply);
//                            } else {
//                                System.out.println("Don't want this sheet by this price!");
//                            }
//                        } else {
//                            System.out.println("maybe time out ");
//                        }
//                    }
//
//                });
//            }
//            sequentialBehaviourCoordinator.addSubBehaviour(parallelBehaviourCoordinator);

//            sequentialBehaviourCoordinator.addSubBehaviour(new Receiver(this, 4000, AgreeOrRefuseTemplate) {
//                @Override
//                public void handle(ACLMessage message) {
//                    if (message != null) {
//
//                        if (message.getPerformative() == ACLMessage.AGREE) {
//                            System.out.println("AGREE from agent " + message.getSender().getLocalName());
//                        } else if (message.getPerformative() == ACLMessage.REFUSE) {
//                            System.out.println("REFUSE from agent " + message.getSender().getLocalName());
//                        }
//                    } else {
//                        block();
//                    }
//                }
//            });
        }
    }

    // --- declaration composite behaviour of CNP protocol

    class TransactionCoordinator extends SequentialBehaviour {

        int bestPrice = Integer.MAX_VALUE;

        List<String> ParticipantNameArray = new ArrayList<>();
        String ConversationID;
        ACLMessage message, reply, bestOfferMessage;

        public TransactionCoordinator(Agent myAgent, ACLMessage message, List<String> participantNameArray) {
            super(myAgent);
            this.message = message;
            this.ConversationID = message.getConversationId();
            this.ParticipantNameArray = participantNameArray;
            System.out.println("ConversationID = " + ConversationID);
        }

        @Override
        public void onStart() {

            addSubBehaviour(new OneShotBehaviour() {

                public void action() {

                    for (String item : ParticipantNameArray) {
                        message.addReceiver(new AID("" + item, AID.ISLOCALNAME));
                    }
                    send(message);
                }
            });

            ParallelBehaviour parallelBehaviourCoordinator = new ParallelBehaviour(myAgent, ParallelBehaviour.WHEN_ALL);

            InformTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId(ConversationID));

            for (int i = 0; i < 3; i++) {
                parallelBehaviourCoordinator.addSubBehaviour(new Receiver(myAgent, 4000, InformTemplate) {
                    public void handle(ACLMessage message) {

                        if (message != null) {
                            int agentOfferPrice = Integer.parseInt(message.getContent());
                            System.out.println("Get message from " + message.getSender().getLocalName() + " by price: " + message.getContent());

                            if (agentOfferPrice < bestPrice) {

                                bestPrice = agentOfferPrice;
                                bestOfferMessage = message;
                            }
                        }
                    }
                });
            }
            addSubBehaviour(parallelBehaviourCoordinator);

            addSubBehaviour(new DelayBehaviour(myAgent, 4000) {
                public void handleElapsedTimeout() {
                    if (bestPrice == Integer.MAX_VALUE) {
                        System.out.println("Don't get any good price");
                    } else {
                        System.out.println("Best price from " + bestOfferMessage.getSender().getLocalName() + " " + bestOfferMessage.getContent());
                        reply = bestOfferMessage.createReply();
                        reply.setPerformative(ACLMessage.REQUEST);
                        send(reply);
                    }

                }
            });

            addSubBehaviour(new Receiver(myAgent, 4000, AgreeOrRefuseTemplate) {
                @Override
                public void handle(ACLMessage message) {
                    if (message != null) {

                        if (message.getPerformative() == ACLMessage.AGREE) {
                            System.out.println("AGREE from agent " + message.getSender().getLocalName());
                        } else if (message.getPerformative() == ACLMessage.REFUSE) {
                            System.out.println("REFUSE from agent " + message.getSender().getLocalName());
                        }
                    }
                }
            });

            addSubBehaviour(new CyclicBehaviour() {
                @Override
                public void action() {
                    message = receive(InformDoneTemplate);
                    if (message != null) {
                        System.out.println("Get INFORM-DONE form agent " + message.getSender().getLocalName());
                    } else {
                        block();
                    }
                }
            });

        }
    }

    // -- additionsl parallel class to catch REFUSE -- //

    class RefuseByIDConverstion extends SimpleBehaviour {

        private boolean finished;

        String ConversationID;
        ACLMessage message;
        MessageTemplate templateIDRefuse;

        public RefuseByIDConverstion(Agent myAgent, String conversationID) {
            super(myAgent);
            this.ConversationID = conversationID;
            this.templateIDRefuse = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REFUSE), MessageTemplate.MatchConversationId(ConversationID));
        }

        @Override
        public void action() {

            message = receive(templateIDRefuse);
            if (message != null) {
                System.out.println("Get REFUSE by ID. Close down!!!!!!!!!!!!!!!!");
                finished = true;
                System.out.println(this.done());

                return;

            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return finished;
        }
    }

    // -- generation new ACLMessage with Conversation ID

    public ACLMessage newACLMessageWithConversationID(int performative) {
        ACLMessage message = new ACLMessage(performative);
        message.setConversationId(genCID());
        return message;
    }

    //  --- generating Conversation IDs -------------------

    protected static int cidCnt = 0;
    private String cidBase;

    public String genCID() {
        if (cidBase == null) {
            cidBase = getLocalName() + hashCode() +
                    System.currentTimeMillis() % 10000 + "_";
        }
        return cidBase + (cidCnt++);
    }
}

