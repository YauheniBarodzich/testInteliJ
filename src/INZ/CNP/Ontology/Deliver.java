package INZ.CNP.Ontology;

import jade.content.AgentAction;

/**
 * Created by Mars on 02.11.2017.
 */
public class Deliver implements AgentAction {
    private Order order;
    private String stock;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }
}
