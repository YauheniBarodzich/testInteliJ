package INZ.CNP;

import INZ.CNP.Behaviour.TransportReponder;
import INZ.CNP.Ontology.Costs;
import INZ.CNP.Ontology.OrderTradingOntology;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.xml.XMLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.Random;

/**
 * Created by Mars on 30.10.2017.
 */
public class Transport extends Agent {

    public static final String RECV_MSG = "received-message";
    public static final String CONV_ID = "conversation-id";

    private Codec xmlCodec = new XMLCodec();
    private Codec codec = new SLCodec();
    private Ontology ontology = OrderTradingOntology.getInstance();

    protected void setup() {
        getContentManager().registerLanguage(xmlCodec);
        getContentManager().registerOntology(ontology);

        addBehaviour(new Transaction());
    }

    class Transaction extends SimpleBehaviour {

        Random rnd = new Random();

        String ConvID;
        MessageTemplate templateCFP = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
        MessageTemplate templateCFPConvID;
        boolean TransactionFinished = false;


        public void action() {

            // 1. sequential Behaviour(get CID) with time out
            // 1.1 when timeout -> restart
            // 1.2 in time -> set up CNP

            SequentialBehaviour ContractNetProtocol = new SequentialBehaviour(myAgent);
            addBehaviour(ContractNetProtocol);
            Behaviour firtPartCNP = new SimpleBehaviour(myAgent) {

                private boolean finished = false;

                public void action() {
                    ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                    if (msg != null) {
                        ContentManager manager = myAgent.getContentManager();

                        try {
//                            System.out.println("MSG: " + msg);
                            ContentElement content = manager.extractContent(msg);
                            System.out.println("CONTENT: " + content);


                            if (content instanceof Costs) {
//                                System.out.println("Content is instance of COSTS");

                                Costs costs = (Costs) content;
                                getDataStore().put(RECV_MSG, msg);
//                                System.out.println("msg.getConversationId ======= " + msg);
                                getDataStore().put(CONV_ID, msg.getConversationId());
//                                System.out.println("msg.getConversationId22222 ======= " + getDataStore().get(CONV_ID));

                                finished = true;
                            } else {
                                System.out.println("Content isn't instance of COSTS");
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

            firtPartCNP.setDataStore(ContractNetProtocol.getDataStore());
            ContractNetProtocol.addSubBehaviour(firtPartCNP);

            Behaviour secPartCNP = new ContractNetResponder(myAgent,
                    MessageTemplate.and( MessageTemplate.MatchPerformative(ACLMessage.CFP), MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET))) {
                //            addBehaviour(new ContractNetResponder(myAgent, templateCFP) {

                @Override
                protected ACLMessage handleCfp(ACLMessage cfp) {
                    System.out.println(myAgent.getLocalName() + ": Handle CFP.ID conversation: " + ((ACLMessage) getDataStore().get("received-message")).getContent());

                    ACLMessage reply = cfp.createReply();
                    // TODO create conditions to accept
                    if (getDataStore().get(CONV_ID).equals(cfp.getConversationId())) {
                        System.out.println(myAgent.getLocalName() + ": Send PROPOSE");
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent("" + rnd.nextInt(100));
                    } else {
                        System.out.println("Send REFUSE");
                        reply.setPerformative(ACLMessage.REFUSE);
                    }
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

        public boolean done() {
            return TransactionFinished;
        }
    }

}
