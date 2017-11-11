package Basics;

/*****************************************************************
 Vendeur1:  First Seller Agent which replies to a QUERY_REF
 --------     with an INFORM with a random "price" [0,100' as
 content.

 Author:  Jean Vaucher
 Date:    Sept 10 2003
 *****************************************************************/

import INZ.Agent.Behaviour.DelayBehaviour;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.Random;


public class Vendeur1 extends Agent
{
    Random rnd = newRandom();
    MessageTemplate template =
            MessageTemplate.MatchPerformative( ACLMessage.QUERY_REF );

    ACLMessage reply;

    protected void setup()
    {
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive( template );
                if (msg!=null) {

                    // we create the reply
                    reply = msg.createReply();
                    reply.setPerformative( ACLMessage.INFORM );
                    reply.setContent("" + rnd.nextInt(100));
                    System.out.print(myAgent.getLocalName() +  " reply by with price " + reply.getContent());
                    int delay = rnd.nextInt( 3000 );
                    System.out.println( " - " +
                            myAgent.getLocalName() + " <- QUERY from " +
                            msg.getSender().getLocalName() +
                            ".  Will answer in " + delay );

                    // but only send it after a random delay

                    addBehaviour(
                            new DelayBehaviour( myAgent, delay)
                            {
                                public void handleElapsedTimeout() {
                                    send(reply); }
                            });
                }
                block();
            }
        });
    }

// ==========================================
// ========== Utility methods ===============
// ==========================================


//  --- generating distinct Random generator -------------------

    Random newRandom()
    {	return  new Random( hashCode() + System.currentTimeMillis()); }

}
