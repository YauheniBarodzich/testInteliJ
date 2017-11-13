package INZ.CNP;

import INZ.CNP.Ontology.*;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.Random;

/**
 * Created by Mars on 30.10.2017.
 */
public class Transport extends Agent {

    public static final String RECV_MSG = "received-message";
    public static final String CONV_ID = "conversation-id";

    private Codec xmlCodec = new XMLCodec();
    private Codec codec = new SLCodec();
    private Ontology OrderOntology = OrderTradingOntology.getInstance();
    private ContentManager manager;


    protected void setup() {
        getContentManager().registerLanguage(xmlCodec);
        getContentManager().registerOntology(OrderOntology);

        registerService();

        addBehaviour(new Transaction());
    }

    class Transaction extends SimpleBehaviour {

        Random rnd = new Random();

        String ConvID;
        MessageTemplate templateCFP = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
        MessageTemplate templateCFPConvID;
        boolean TransactionFinished = false;
        ContentManager manager;

        public void action() {

            SequentialBehaviour ContractNetProtocol = new SequentialBehaviour(myAgent);
            addBehaviour(ContractNetProtocol);
            Behaviour firstPartCNP = new SimpleBehaviour(myAgent) {

                private boolean finished = false;

                public void action() {
                    ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                    if (msg != null) {
                        System.out.println(myAgent.getLocalName() + ": get INFORM msg");

                        try {
                            manager = myAgent.getContentManager();
                            ContentElement content = manager.extractContent(msg);

                            if (content instanceof IsBusy) {
                                System.out.println(myAgent.getLocalName() + ": IsBusy instance");

                                IsBusy isBusyContent = (IsBusy) content;
                                getDataStore().put(RECV_MSG, msg);
                                getDataStore().put(CONV_ID, isBusyContent.getConversationID());

                                finished = true;
                            } else {
                                System.out.println(myAgent.getLocalName() + ": isn't instance of IsBusy.class");
                            }

                        } catch (Exception ex) {
                            System.out.println(" Transport: ***END EXCEPTION***");
                            ex.printStackTrace();
                        }
                    } else {
                        block();
                    }
                }

                public boolean done() {
                    return finished;
                }
            };

            firstPartCNP.setDataStore(ContractNetProtocol.getDataStore());
            ContractNetProtocol.addSubBehaviour(firstPartCNP);

            Behaviour secPartCNP = new ContractNetResponder(myAgent,
                    MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET))) {

                protected ACLMessage handleCfp(ACLMessage cfp) {
                    System.out.println(myAgent.getLocalName() + ": Handle CFP");

                    ACLMessage reply = cfp.createReply();

                    if (!getDataStore().get((String) CONV_ID).equals(cfp.getConversationId())) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        return reply;
                    }

                    try {
                        ContentElement content = manager.extractContent(cfp);

                        if (content instanceof Deliver) {
                            System.out.println(myAgent.getLocalName() + ": Send PROPOSE");

                            Deliver deliver = (Deliver) content;
                            Order order = deliver.getOrder();
                            // TODO criteries to accept or reject CFP
                            if (true) {
                                Costs costs = new Costs();
                                costs.setItem(order);
                                costs.setPrice(rnd.nextInt(100));
                                reply.setPerformative(ACLMessage.PROPOSE);
                                reply.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                                try {
                                    reply.setLanguage(XMLCodec.NAME);
                                    reply.setOntology(OrderOntology.getName());
                                    manager.fillContent(reply, costs);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                            } else {
                                System.out.println("Send REFUSE");
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
//                    // TODO create conditions to accept
//                    if (getDataStore().get(CONV_ID).equals(cfp.getConversationId())) {
//
//                    } else {
//                        System.out.println("Send REFUSE");
//                        reply.setPerformative(ACLMessage.REFUSE);
//                    }
                    return reply;
                }

                @Override
                protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                    System.out.println(myAgent.getLocalName() + ": get REJECT-PROPOSAL from " + reject.getSender().getLocalName());
                    TransactionFinished = true;
                }

                @Override
                protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                    System.out.println(myAgent.getLocalName() + ": get ACCEPT-PROPOSAL from " + accept.getSender().getLocalName() + ": " + propose.getContent());
                    ACLMessage reply = accept.createReply();
                    reply.setPerformative(ACLMessage.INFORM);

                    TransactionFinished = true;

                    return reply;
                }


            };

            secPartCNP.setDataStore(ContractNetProtocol.getDataStore());
            ContractNetProtocol.addSubBehaviour(secPartCNP);
        }

        public int onEnd() {
            reset();
            myAgent.addBehaviour(this);
            return 0;
        }

        public boolean done() {
            return TransactionFinished;
        }
    }

    public void registerService() {
        System.out.println(this.getLocalName() + ": registerService()");

        // Register deliver service in yellow pages
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());
        agentDescription.addLanguages(xmlCodec.getName());
        agentDescription.addOntologies(OrderOntology.getName());
        agentDescription.addProtocols(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("DELIVER");
        serviceDescription.setName(getLocalName() + "-DELIVER");

        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return;
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        // Deregister from yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

}
