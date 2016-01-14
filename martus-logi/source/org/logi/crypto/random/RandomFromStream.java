// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.random;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.logi.crypto.Crypto;

/**
 * This class reads bits from an InputStream object and returns them
 * as random values. No randomness checking is done and an Error is
 * thrown if the end of the Reader is ever reached.
 * <p>
 * This class is useful f.ex. for reding random bits from the
 * <code>/dev/random</code> or <code>/dev/urandom</code> devices
 * where they are available (such as in linux). This would be done with
 * the following code:
 * <blockquote><pre>
 * Random rand;
 * try {
 *   rand=new RandomFromReader(new FileInputStream("/dev/random"));
 * } catch (FileNotFoundException e) {
 *   rand=new RandomSpinner();
 * }
 * </pre></blockquote>
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 *         (<a href="mailto:logir@logi.org">logir@logi.org</a>) */
public class RandomFromStream extends Random
{

    private InputStream in;

    private static String emptyMsg = "RandomFromStream ran out of random bytes.";

    /**
     * Create a new RandomFromStream obejct. Random bits are read from
     * <code>in</code>
     */
    public RandomFromStream(InputStream in)
    {
        this.in=in;
    }

    /** Generates a user specified number of random bytes. */
    public void nextBytes(byte[] bytes)
    {
        try {
            if(Crypto.readBlock(in, bytes, 0, bytes.length) == -1)
                throw new Error(emptyMsg);
        } catch (IOException e) {
            throw new Error(emptyMsg);
        }
    }

    /** Generates the next random number. */
    protected synchronized int next(int bits)
    {
        try {
            int r=0;
            int b=0;
            while(b<bits) {
                int s = in.read();
                if(s<0)
                    throw new Error(emptyMsg);
                r = (r<<8) | s;
                b+=8;
            }
            return r >>> (b-bits);
        } catch (IOException e) {
            throw new Error(emptyMsg);
        }
    }

}
