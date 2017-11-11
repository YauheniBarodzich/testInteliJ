package Basics;
/*****************************************************************
 Acheteur1:  First Buyer Agent
 ----------
 Author:  Jean Vaucher
 Date:    Sept 10 2003
 *****************************************************************/

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.*;

import java.util.Random;


public class Acheteur1 extends Agent
{
    Random rnd = new Random( hashCode());

    MessageTemplate template ;

    int        bestPrice = 9999;
    ACLMessage bestOffer = null;

    protected void setup()
    {
        ACLMessage msg = newMsg( ACLMessage.QUERY_REF );

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                MessageTemplate.MatchConversationId( msg.getConversationId() ));

        SequentialBehaviour seq = new SequentialBehaviour();
        addBehaviour( seq );

        ParallelBehaviour par = new ParallelBehaviour( ParallelBehaviour.WHEN_ALL );
        seq.addSubBehaviour( par );

        for (int i = 1; i<=3; i++)
        {
            msg.addReceiver( new AID( "s" + i,  AID.ISLOCALNAME ));

            par.addSubBehaviour( new myReceiver( this, 2000, template)
            {
                public void handle( ACLMessage msg)
                {
                    if (msg != null) {
                        int offer = Integer.parseInt( msg.getContent());
                        if (offer < bestPrice) {
                            bestPrice = offer;
                            bestOffer = msg;
                        }  }
                }
            });
        }


        seq.addSubBehaviour( new OneShotBehaviour()
        {
            public void action()
            {
                if (bestOffer != null)
                    System.out.println("Best Price $" + bestPrice );
                else
                    System.out.println("Got no quotes");
            }
        });

        send ( msg );
        System.out.println("b1: sent msg");

    }
//    {
//        ACLMessage msg = newMsg( ACLMessage.QUERY_REF, "",
//                new AID( "s1", AID.ISLOCALNAME) );
//
//        template = MessageTemplate.and(
//                MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
//                MessageTemplate.MatchConversationId( msg.getConversationId() ));
//
//        addBehaviour( new myReceiver(this, 10000, template )
//        {
//            public void handle( ACLMessage msg )
//            {
//                if (msg == null)
//                    System.out.println("Buyer: Timeout");
//                else
//                    System.out.println("Buyer received: $"+ msg);
//
//            }
//        });
//
//        send ( msg );
//
//
//    }

// ========== Utility methods =========================

//  --- generating Conversation IDs -------------------

    protected static int cidCnt = 0;
    String cidBase ;

    String genCID()
    {
        if (cidBase==null) {
            cidBase = getLocalName() + hashCode() +
                    System.currentTimeMillis()%10000 + "_";
        }
        return  cidBase + (cidCnt++);
    }

//  --- Methods to initialize ACLMessages -------------------

    ACLMessage newMsg( int perf, String content, AID dest)
    {
        ACLMessage msg = newMsg(perf);
        if (dest != null) msg.addReceiver( dest );
        msg.setContent( content );
        return msg;
    }

    ACLMessage newMsg( int perf)
    {
        ACLMessage msg = new ACLMessage(perf);
        msg.setConversationId( genCID() );
        return msg;
    }


}
