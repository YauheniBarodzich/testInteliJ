package INZ.CNP.Ontology;

import jade.content.Concept;
import jade.util.leap.List;

/**
 * Created by Mars on 02.11.2017.
 */
public class Order implements Concept {
    private String initiator;
    private String executor;
    private String client;
    private String id;
    private List products;


    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List getProducts() {
        return products;
    }

    public void setProducts(List products) {
        this.products = products;
    }

    public String toString() {
        return "initiator: " + initiator + "executor: " + "client:" + client + "id:" + id;
    }
}
