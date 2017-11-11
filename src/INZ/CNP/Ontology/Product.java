package INZ.CNP.Ontology;

import jade.content.Concept;

/**
 * Created by Mars on 05.11.2017.
 */
public class Product implements Concept {
    private String idP;
    private String description;
    // TODO imaging some criteria to item

    public String getIdP() {
        return idP;
    }

    public void setIdP(String idP) {
        this.idP = idP;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return "idP" + idP + "description: " + description;
    }
}
