package INZ.CNP.Ontology;

import jade.content.Predicate;
import jade.content.schema.PredicateSchema;

/**
 * Created by Mars on 02.11.2017.
 */
public class Costs implements Predicate {

    private Order item;
    private int price;

    public void setItem(Order item) {
        this.item = item;
    }

    public Order getItem() {
        return item;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }
}
