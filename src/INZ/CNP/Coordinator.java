package INZ.CNP;

import INZ.CNP.Ontology.Costs;
import INZ.CNP.Ontology.Order;
import INZ.CNP.Ontology.OrderTradingOntology;
import INZ.CNP.Ontology.Product;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.util.leap.*;

import java.util.Vector;
import jade.util.leap.List;


/**
 * Created by Mars on 30.10.2017.
 */

// -- Initiator of CNP -- //

public class Coordinator extends Agent {

    public static final String CONV_ID = "conversation-id";

    private Codec xmlCodec = new XMLCodec();
    //    private Codec codec = new SLCodec();
    private Ontology ontology = OrderTradingOntology.getInstance();
    Order order;
    Costs costs;
    Product product;

    protected void setup() {


        getContentManager().registerLanguage(xmlCodec);
        getContentManager().registerOntology(ontology);

        SequentialBehaviour ContactNetProtocol = new SequentialBehaviour(this);

        Behaviour firstPart = new SimpleBehaviour(this) {
            private boolean finished = false;

            public void action() {
                ContentManager manager = myAgent.getContentManager();

                order = new Order();

                order.setClient("myClient");
                order.setExecutor("myExecutor");
                order.setInitiator("myInitiator");
                order.setId("ID_1");

                product = new Product();
                product.setIdP("ID-Product-1");
                product.setDescription("some desc");

                List list = new ArrayList();

                list.add(product);
                order.setProducts(list);

                costs = new Costs();
                costs.setItem(order);
                costs.setPrice(33);

                ACLMessage msgConvID = new ACLMessage(ACLMessage.INFORM);
                msgConvID.setLanguage(XMLCodec.NAME);
                msgConvID.setOntology(ontology.getName());

                try {
                    getDataStore().put(CONV_ID, genCID());

                    msgConvID.setConversationId((String) getDataStore().get(CONV_ID));

                    System.out.println("C_0: SET CONV ID " + (String) getDataStore().get(CONV_ID));

                    msgConvID.addReceiver(new AID("T_0", AID.ISLOCALNAME));
                    msgConvID.addReceiver(new AID("T_1", AID.ISLOCALNAME));
                    msgConvID.addReceiver(new AID("T_2", AID.ISLOCALNAME));
                    msgConvID.addReceiver(new AID("T_3", AID.ISLOCALNAME));
                    manager.fillContent(msgConvID, costs);

                    send(msgConvID);
                } catch (Exception ex) {
                    System.out.println(" Coordinator: ***EXCEPTION***");
                    ex.printStackTrace();
                }
//                msgConvID.setContent(genCID());
//                msgConvID.addReceiver(new AID("T_0", AID.ISLOCALNAME));
//                msgConvID.addReceiver(new AID("T_1", AID.ISLOCALNAME));
//                msgConvID.addReceiver(new AID("T_2", AID.ISLOCALNAME));
//
//                send(msgConvID);
//
//                msgConvID = new ACLMessage(ACLMessage.INFORM);
//                msgConvID.setContent(genCID());
//                msgConvID.addReceiver(new AID("T_3", AID.ISLOCALNAME));
//
//                send(msgConvID);
                finished = true;
            }

            public boolean done() {
                return finished;
            }
        };

        firstPart.setDataStore(ContactNetProtocol.getDataStore());
        ContactNetProtocol.addSubBehaviour(firstPart);

        Behaviour secPart = new ContractNetInitiator(this, null) {

            ACLMessage bestOffer = null;
            int bestPrice = Integer.MAX_VALUE;

            protected Vector prepareCfps(ACLMessage cfp) {

                cfp = new ACLMessage(ACLMessage.CFP);
                cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                cfp.setConversationId((String) getDataStore().get(CONV_ID));

                cfp.addReceiver(new AID("T_0", AID.ISLOCALNAME));
                cfp.addReceiver(new AID("T_1", AID.ISLOCALNAME));
                cfp.addReceiver(new AID("T_2", AID.ISLOCALNAME));
                cfp.addReceiver(new AID("T_3", AID.ISLOCALNAME));

                Vector v = new Vector();
                v.add(cfp);
                return v;
            }

            protected void handlePropose(ACLMessage propose, Vector acceptances) {
                System.out.println(myAgent.getLocalName() + ": get PROPOSE from " + propose.getSender().getLocalName() + ": " + propose.getContent());
//                int price = Integer.parseInt(propose.getContent());
//                if (bestOffer == null || price < bestPrice) {
//                    bestOffer = propose;
//                    bestPrice = price;
//                    ACLMessage accept = bestOffer.createReply();
//                    System.out.println("***Best offer*** from " + propose.getSender().getLocalName());
//
//                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//                    acceptances.add(accept);
//
//                } else {
//                    ACLMessage reply = propose.createReply();
//                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
//                    acceptances.add(reply);
//                }
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println(myAgent.getLocalName() + ": get REFUSE from " + refuse.getSender().getLocalName() + ": " + refuse.getContent());
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println(myAgent.getLocalName() + ": get INFORM from " + inform.getSender().getLocalName());
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {

                System.out.println("handleAllResponses() size= " + responses.size());

                ACLMessage response;

                for (int i = 0; i < responses.size(); i++) {

                    try {
                        response = (ACLMessage) responses.get(i);
                    } catch (Exception ex) {
                        System.out.println("Exception!");
                        ex.getStackTrace();
                        return;
                    }

                    if (ACLMessage.PROPOSE == response.getPerformative()) {

                        int price = Integer.parseInt(response.getContent());
                        if (bestOffer == null || price < bestPrice) {
                            bestOffer = response;
                            bestPrice = price;
//                            ACLMessage newMsg= new ACLMessage(ACLMessage.INFORM);
//                            newMsg.setOntology();

//                            ACLMessage accept = bestOffer.createReply();
//                            System.out.println("***Best offer*** from " + response.getSender().getLocalName());

//                            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//                            acceptances.add(accept);

                        } else {
//                            ACLMessage reply = response.createReply();
//                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
//                            acceptances.add(reply);
                        }
                    }
                }

                for (int i = 0; i < responses.size(); i++) {
                    response = (ACLMessage) responses.get(i);
                    if (response == bestOffer) {
                        try {
                            System.out.println("$$$ BEST OFFER $$$ from " + bestOffer.getSender().getLocalName() + " by price " + bestOffer.getContent());
                        } catch (Exception ex) {
                            System.out.println("Exception $$$ BEST OFFER $$$");
                            System.out.print(ex.getMessage());
                        }

                        bestOffer = bestOffer.createReply();
                        bestOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);


                        acceptances.add(bestOffer);

//                        System.out.println("$$$ BEST OFFER $$$ from " + bestOffer.getSender().getLocalName() + " price: " + bestOffer.getContent());

                    } else {
                        System.out.println(myAgent.getLocalName() + ": SEND REJECT to " + response.getSender().getLocalName());
                        response = response.createReply();
                        response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptances.add(response);
                    }

                }
            }
        };

        secPart.setDataStore(ContactNetProtocol.getDataStore());
        ContactNetProtocol.addSubBehaviour(secPart);


        addBehaviour(ContactNetProtocol);
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
