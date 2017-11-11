package Basics;

import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.core.*;

public class DelayBehaviour1 extends SimpleBehaviour {
    private long timeout, wakeupTime;
    private boolean finished;

    public DelayBehaviour1(Agent a, long timeout) {
        super(a);
        this.timeout = timeout;
        finished = false;
    }

    @Override
    public int onEnd() {
        System.out.println("On END");
        return super.onEnd();
    }

    public void onStart() {
        wakeupTime = System.currentTimeMillis() + timeout;
    }

    public void action() {
        long dt = wakeupTime - System.currentTimeMillis();
        if (dt <= 0) {
            finished = true;
            handleElapsedTimeout();
        } else
            block(dt);

    } //end of action

    protected void handleElapsedTimeout() {
    }

    public void reset(long timeout) {
        wakeupTime = System.currentTimeMillis() + timeout;
        finished = false;
    }

    public boolean done() {
        return finished;
    }
}