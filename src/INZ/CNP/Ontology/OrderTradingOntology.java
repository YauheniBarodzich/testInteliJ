package INZ.CNP.Ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.*;

/**
 * Created by Mars on 02.11.2017.
 */
public class OrderTradingOntology extends Ontology implements OrderTradingVocabulary {

    public static final String ONTOLOGY_NAME = "Order-trading-ontology";

    private static Ontology theInstance = new OrderTradingOntology();

    public static Ontology getInstance() {
        return theInstance;
    }

    private OrderTradingOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try {
            add(new ConceptSchema(ORDER), Order.class);
            add(new ConceptSchema(PRODUCT), Product.class);
            add(new PredicateSchema(COSTS), Costs.class);
            add(new AgentActionSchema(DELIVER), Deliver.class);
            add(new PredicateSchema(IS_BUSY), IsBusy.class);

            // Structure of the schema for the Order concept
            ConceptSchema conceptORDER = (ConceptSchema) getSchema(ORDER);
            conceptORDER.add(ORDER_INITIATOR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptORDER.add(ORDER_EXECUTOR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptORDER.add(ORDER_CLIENT, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptORDER.add(ORDER_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptORDER.add(ORDER_PRODUCTS, (ConceptSchema) getSchema(PRODUCT), 0, ObjectSchema.UNLIMITED);

            // Structure of the schema for the PRODUCT concept
            ConceptSchema conceptPRODUCT = (ConceptSchema) getSchema(PRODUCT);
            conceptPRODUCT.add(PRODUCT_IDP, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptPRODUCT.add(PRODUCT_DESCRIPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // Structure of the schema for the Costs predicate
            PredicateSchema predicateCOSTS = (PredicateSchema) getSchema(COSTS);
            predicateCOSTS.add(COSTS_ITEM, (ConceptSchema) getSchema(ORDER));
            predicateCOSTS.add(COSTS_PRICE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            // Structure of the schema for the Deliver AgentAction
            AgentActionSchema actionDeliver = (AgentActionSchema) getSchema(DELIVER);
            actionDeliver.add(DELIVER_ORDER, (ConceptSchema) getSchema(ORDER));
            actionDeliver.add(DELIVER_STOCK, (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // Structure of the schema for the IsBusy Predicate
            PredicateSchema predicateIS_BUSY = (PredicateSchema) getSchema(IS_BUSY);
            predicateIS_BUSY .add(ISBUSY_CONVERSATION_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));

        } catch (OntologyException ex) {
            ex.printStackTrace();
        }
    }
}
