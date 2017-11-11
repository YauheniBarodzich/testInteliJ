package Basics;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.Random;


public class Test1 extends Agent
{
    long t0;
    Random  rnd = new Random(hashCode());
//    MessageTemplate mt = MessageTemplate.MatchPerformative( ACLMessage.INFORM );

    protected void setup()
    {
        t0 = System.currentTimeMillis();

        addBehaviour( new TickerBehaviour( this, 2000 ) {

            int n = 0;

            protected void onTick()
            {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver( myAgent.getAID() );
//                System.out.println("Test1 : onTick() -> getAID " + myAgent.getAID().getName());
                msg.setContent( "#" + n );
                myAgent.send(msg);
                if (n > 5) {
                    return;
                }else {
                    System.out.println("n = " + n);
                    n++;
                }

            }
        });
//
//        addBehaviour( new myReceiver( this, 1000 )
//        {
//            public void handle( ACLMessage msg) {
//                System.out.println("R1:");
//                if (msg==null) {
//                    System.out.println("Timeout:" + time());
//                    reset(500);
//                }
//                else
//                    dumpMessage( msg );
//            }
//        });
//        addBehaviour( new myReceiver( this, 3000 )
//        {
//            public void handle( ACLMessage msg) {
//                System.out.println("R2:");
//                if (msg==null)
//                    System.out.println("Timeout at 3000");
//                else
//                    dumpMessage( msg );
//            }
//        });
//        addBehaviour( new myReceiver( this, 4000 )
//        {
//            public void handle( ACLMessage msg) {
//                System.out.println("R3:");
//                if (msg==null)
//                    System.out.println("Timeout at 4000");
//                else
//                    dumpMessage( msg );
//            }
//        });
    }


    void dumpMessage( ACLMessage msg )
    {
        System.out.println( time() + ": "
                + getLocalName() + " gets "
                + ACLMessage.getPerformative(msg.getPerformative() )
                + " from "
                +  msg.getSender().getLocalName()
                + ", content: " +  msg.getContent()
                + ", cid=" + msg.getConversationId());
    }

    int time() { return (int)(System.currentTimeMillis()-t0); }

}