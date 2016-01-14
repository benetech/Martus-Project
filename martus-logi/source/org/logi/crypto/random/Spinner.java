// This file is in the public domain. Do with it as you wish.

package org.logi.crypto.random;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper class for the PureSpinner class. This code is in the public domain.
 *
 * @version 1.1.1
 * @see org.logi.crypto.random.PureSpinner
 */
public class Spinner extends Thread
{

    static class Bool
    {

        private boolean bool;

        public Bool(boolean bool)
        {
            this.bool = bool;
        }

        public synchronized void set(boolean bool)
        {
            this.bool = bool;
        }

        public synchronized boolean get()
        {
            return bool;
        }
    }


    /** Return the number of spins performed in t milliseconds. */
    public static int spin(long t)
    {
        int counter = 0;
        Bool done = new Bool(false);
        Thread s = new Spinner(t, done);
        s.start();
        do {
            ++counter;
            Thread.yield();
        } while (!done.get());
        return counter;
    }

    private long t;
    private Bool done;

    /** Create a new Spinner which spins for t milliseconds each time it is called. */
    private Spinner(long t, Bool done)
    {
        setPriority(getPriority() + 1);
        this.t=t;
        this.done = done;
    }

    /** Sleep for the specified amount and then die. */
    public void run()
    {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ex) {}
        done.set(true);
    }

    /**
     * Returns t such that spin(t) is larger than n. This value may change as
     * the load of the system changes.
     */
    public static int guessTime(int n)
    {
        int t=5;
        while(spin(t)<n)
            t=(t*3)/2;
        return t;
    }

    /**
     * Call with optional parameter t.
     * <p>
     * Calls spin(t) 2^20 times and outputs the 8 lowest-order bits to a
     * file named "spin.t", where t is replaced with the value of the
     * parameter t. 
     * <p>
     * If t is omitted, t=guessTime(1024) will be used.
     * <p>
     * the output of this program can be compressed to estimate the entropy
     * of the random number generator. On my system the output does not
     * compress at all for t>=5.
     */
    public static void main(String[] arg) throws IOException
    {
        int t;
        if(arg.length > 0)
            t = Integer.parseInt(arg[0]);
        else {
            t = guessTime(1024);
        }
        System.out.println("Using t="+t);
        DataOutputStream out = new DataOutputStream(new FileOutputStream("spin."+t));
        for(int i=0; i<1024*1024; i++) {
            int n = spin(t);
            out.writeByte(n);
            if(i%128 == 0)
                System.out.println(i+"\t"+n);
        }
        System.out.println(1024);
        out.flush();
    }
}
