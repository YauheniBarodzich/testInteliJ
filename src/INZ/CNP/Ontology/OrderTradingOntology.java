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
//            add(new AgentActionSchema(DELIVER), Deliver.class);

            // Structure of the schema for the Order concept
            ConceptSchema conceptSchemaORDER = (ConceptSchema) getSchema(ORDER);
            conceptSchemaORDER.add(ORDER_INITIATOR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptSchemaORDER.add(ORDER_EXECUTOR, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptSchemaORDER.add(ORDER_CLIENT, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptSchemaORDER.add(ORDER_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptSchemaORDER.add(ORDER_PRODUCTS, (ConceptSchema) getSchema(PRODUCT),0, ObjectSchema.UNLIMITED);

            // Structure of the schema for the PRODUCT concept
            ConceptSchema conceptSchemaPRODUCT = (ConceptSchema) getSchema(PRODUCT);
            conceptSchemaPRODUCT.add(PRODUCT_IDP, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            conceptSchemaPRODUCT.add(PRODUCT_DESCRIPTION, (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // Structure of the schema for the Costs predicate
            PredicateSchema predicateSchemaCOSTS = (PredicateSchema) getSchema(COSTS);
            predicateSchemaCOSTS.add(COSTS_ITEM, (ConceptSchema) getSchema(ORDER));
            predicateSchemaCOSTS.add(COSTS_PRICE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));


            // Structure of the schema for the Deliver AgentAction
//            AgentActionSchema actionSchema = (AgentActionSchema) getSchema(DELIVER);
//            actionSchema.add(DELIVER_ORDER, (ConceptSchema) getSchema(ORDER));
//            actionSchema.add(DELIVER_EXECUTOR, (PrimitiveSchema) getSchema(BasicOntology.STRING));

        } catch (OntologyException ex) {
            ex.printStackTrace();
        }
        ;

    }
}
