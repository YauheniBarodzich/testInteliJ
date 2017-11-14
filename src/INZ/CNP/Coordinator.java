package INZ.CNP;

import INZ.Agent.Behaviour.DelayBehaviour;
import INZ.CNP.Ontology.*;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.*;

import java.util.Date;
import java.util.Timer;
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

    private List TransportAgentList = new ArrayList();

    protected void setup() {
//////////////////*************************///////////////////////
        ACLMessage DFmessage = createACLSubscribeMessage(this);
        Behaviour b = new SubscribeBehaviour(this, DFmessage);
        addBehaviour(b);
//////////////////*************************///////////////////////


        // Update list of agent Tranport every 1s
        addBehaviour(new getAgentsBehaviour(this));

        getContentManager().registerLanguage(xmlCodec);
        getContentManager().registerOntology(ontology);

        SequentialBehaviour ContactNetProtocol = new SequentialBehaviour(this);

        // Is Agent busy? + send ConvID;
        Behaviour firstPart = new SimpleBehaviour(this) {
            private boolean finished = false;

            public void action() {
                ContentManager manager = myAgent.getContentManager();

                try {
                    getDataStore().put(CONV_ID, genCID());
                    IsBusy agentAvailability = new IsBusy();
                    agentAvailability.setConversationID((String) getDataStore().get(CONV_ID));

                    ACLMessage msgConvID = new ACLMessage(ACLMessage.INFORM);
                    msgConvID = addReceiversToMessage(msgConvID, TransportAgentList);
                    msgConvID.setLanguage(XMLCodec.NAME);
                    msgConvID.setOntology(ontology.getName());
                    manager.fillContent(msgConvID, agentAvailability);

                    send(msgConvID);
                    System.out.println("KR sent inform msg");
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

                ContentManager manager = myAgent.getContentManager();

                Deliver deliver = createDeliveryOrder();

                cfp = new ACLMessage(ACLMessage.CFP);
                cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                cfp.setConversationId((String) getDataStore().get(CONV_ID));

                cfp = addReceiversToMessage(cfp, TransportAgentList);

                try {
                    cfp.setLanguage(XMLCodec.NAME);
                    cfp.setOntology(ontology.getName());
                    manager.fillContent(cfp, deliver);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Vector v = new Vector();
                v.add(cfp);
                return v;
            }

            protected void handlePropose(ACLMessage propose, Vector acceptances) {
                System.out.println(myAgent.getLocalName() + ": get PROPOSE from " + propose.getSender().getLocalName() + ": " + propose.getContent());
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println(myAgent.getLocalName() + ": get REFUSE from " + refuse.getSender().getLocalName());
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println(myAgent.getLocalName() + ": get INFORM from " + inform.getSender().getLocalName());
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                System.out.println("handleAllResponses() size= " + responses.size());

                ContentManager manager = myAgent.getContentManager();
                ContentElement content;

                ACLMessage response;

                for (int i = 0; i < responses.size(); i++) {

                    try {
                        response = (ACLMessage) responses.get(i);
                        content = manager.extractContent(response);
                    } catch (Exception ex) {
                        System.out.println("Exception!");
                        ex.getStackTrace();
                        return;
                    }

                    if (ACLMessage.PROPOSE == response.getPerformative() && content instanceof Costs) {

                        int price = ((Costs) content).getPrice();
                        if (bestOffer == null || price < bestPrice) {
                            bestOffer = response;
                            bestPrice = price;
                        } else {
                        }
                    }
                }

                for (int i = 0; i < responses.size(); i++) {
                    response = (ACLMessage) responses.get(i);
                    if (response == bestOffer) {
                        try {
                            System.out.println(myAgent.getLocalName() + ": $$$ BEST OFFER $$$ from " + bestOffer.getSender().getLocalName() + " by price " + bestOffer.getContent());
                        } catch (Exception ex) {
                            System.out.println(myAgent.getLocalName() + ": Exception $$$ BEST OFFER $$$");
                            System.out.print(ex.getMessage());
                        }
                        bestOffer = bestOffer.createReply();
                        bestOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                        acceptances.add(bestOffer);

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

    /*********** Subscribe behaviour **********/
    public ACLMessage createACLSubscribeMessage(Agent agent){
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();

        serviceDescription.setType("DELIVER");
        template.addOntologies(ontology.getName());
        template.addLanguages(xmlCodec.getName());

        template.addServices(serviceDescription);

        return DFService.createSubscriptionMessage(agent, getDefaultDF(), template, null);
    }

    //
    class SubscribeBehaviour extends SubscriptionInitiator {
        SubscribeBehaviour(Agent agent, ACLMessage msg) {
            super(agent, msg);
        }
        // get code form https://www.programcreek.com
        protected void handleInform(ACLMessage inform) {
            System.out.println("***** Agent "+getLocalName()+": Notification received from DF ******");
            try {
                DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
                if (results.length > 0) {
                    for (int i = 0; i < results.length; ++i) {
                        DFAgentDescription dfd = results[i];
                        AID provider = dfd.getName();
                        // The same agent may provide several services; we are only interested
                        // in the weather-forcast one
                        Iterator it = dfd.getAllServices();
                        while (it.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) it.next();
//                            if (sd.getType().equals("weather-forecast")) {
//                                System.out.println("Weather-forecast service for Italy found:");
                                System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
//                            }
                        }
                    }
                } else {
                    System.out.println("*** size is < 0");
                }
                System.out.println();
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

    }


    //********* "Get agent form yellow pages" Behaviour **************/

    class getAgentsBehaviour extends OneShotBehaviour {
//
//        public getAgentsBehaviour(Agent agent, long period){
//            super(agent, period);
//        }

        getAgentsBehaviour(Agent agent) {
            super(agent);
        }

        @Override
        public void action() {
            //Update list of Transport agents
            System.out.println(myAgent.getLocalName() + ": onTick()");

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription serviceDescription = new ServiceDescription();

            serviceDescription.setType("DELIVER");
            template.addOntologies(ontology.getName());
            template.addLanguages(xmlCodec.getName());

            template.addServices(serviceDescription);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                System.out.println(result.length);
                TransportAgentList.clear();

                for (int i = 0; i < result.length; i++) {
                    System.out.println(result[i].getName());
                    TransportAgentList.add(result[i].getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
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

    // --- generate delivery of order ---//
    public Deliver createDeliveryOrder() {

        Order order = new Order();
        order.setClient("myClient");
        order.setExecutor("myExecutor");
        order.setInitiator("myInitiator");
        order.setId("ID_1");

        Product product = new Product();
        product.setIdP("ID-Product-1");
        product.setDescription("some desc");

        List list = new ArrayList();
        list.add(product);
        order.setProducts(list);

        Costs costs = new Costs();
        costs.setItem(order);
        costs.setPrice(33);

        Deliver deliveryOfOrder = new Deliver();

        deliveryOfOrder.setOrder(order);
        deliveryOfOrder.setStock("ST1");
        return deliveryOfOrder;
    }

    // --- add Receivers ---//
    public ACLMessage addReceiversToMessage(ACLMessage msg, List receivers) {

        for (int i = 0; i < receivers.size(); i++) {
            msg.addReceiver((AID) receivers.get(i));
            System.out.println(this.getLocalName() + ": " + (AID) receivers.get(i));
        }
        return msg;
    }
}
