// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto.random;
import java.util.Random;

/**
 * This class uses the scheduler to gather entropy.
 * <p>
 * To generate one byte of data it launches a thread which counts in a
 * tight loop. After a certain time, this thread is killed and the 8
 * lowest order bytes of the counter are returned.
 * <p>
 * The spin-period is set when a PureSpinner object is created. It is
 * chosen so that the counter will reach at least 1024 when a byte is
 * generated. However, under excessive load the counter may not count
 * much above 256.
 * <p>
 * The output of this RNG is may not have very good statistical properties,
 * but each byte of data should contain one or two bits of entropy. It
 * should not be used directly, but rather to gather entropy for another
 * PRNG class such as the RandomSpinner class.
 * <p>
 * The helper class which does the actual number generation is based on code by
 * Henry Strickland (<a href="mailto:strix@versant.com">strix@versant.com</a>)
 * and Greg Noel (<a href="mailto:greg@qualcomm.com">greg@qualcomm.com</a>).
 * This was in turn based on similar C code by Matt Blaze, Jack Lacy, and
 * Don Mitchell.
 *
 * @see org.logi.crypto.random.RandomMD5
 * @see org.logi.crypto.random.Spinner
 * @version 1.0.6
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class PureSpinner extends Random
{

    private int t;

    public PureSpinner()
    {
        t=Spinner.guessTime(1024);
    }

    /** Generates a user specified number of random bytes. */
    public void nextBytes(byte[] bytes)
    {
        for(int i=bytes.length-1; i>=0; i--)
            bytes[i]=(byte)Spinner.spin(t);
    }

    /** Generates the next random number. */
    protected synchronized int next(int bits)
    {
        int b=0;
        int r=0;
        while(b<bits) {
            r = (r<<8) | Spinner.spin(t);
            b+=8;
        }
        if(bits>=32)
            return r;
        int mask = (1 << bits)-1;
        return r & mask;
    }

}
