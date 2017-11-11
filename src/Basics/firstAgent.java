package Basics;

import jade.core.Agent;
import jade.core.behaviours.*;

public class firstAgent extends Agent {

    long t0 ;
    long time() { return System.currentTimeMillis()-t0; }

    protected void setup()
    {
        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour( new DelayBehaviour1( this, 250 ) );
        seq.addSubBehaviour( new OneShotBehaviour(this)
        {
            public void action() {
                System.out.println( time() + ": " + "... Message1");
            }
        });

        seq.addSubBehaviour( new DelayBehaviour1( this, 500 )
        {
            protected void handleElapsedTimeout() {
                System.out.println( time() + ": " + "  ...and then Message 2");

                myAgent.doDelete();
                System.exit(1);
            }
        });
        addBehaviour( seq );

        addBehaviour( new TickerBehaviour( this, 300 )
        {
            protected void onTick() {
                System.out.println( System.currentTimeMillis()-t0 +
                        ": " + myAgent.getLocalName());
            }
        }) ;

        t0 = System.currentTimeMillis();
    }}