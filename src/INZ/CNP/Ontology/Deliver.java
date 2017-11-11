package INZ.CNP.Ontology;

import jade.content.AgentAction;

/**
 * Created by Mars on 02.11.2017.
 */
public class Deliver implements AgentAction {
    private Order order;
    private String executor;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }
}
