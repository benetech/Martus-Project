// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.random;
import java.security.SecureRandom;

import org.logi.crypto.Crypto;
import org.logi.crypto.sign.MD5State;

/**
 * This class implements an MD5-based PRNG and must be seeded from another
 * RNG such as PureSpinner.
 * <p>
 * Random bytes are generated in blocks of 16 bytes. Output-block number i
 * is <code>r_i = H(s_0...s_i)</code>. <code>s_0</code> is the initial seed
 * which is permuted in each round to form <code>s_i = s_{i-1} + r_i</code>
 * and <code>H</code> is the MD5 hash-function where the final step of
 * appending the length of the message is omitted.
 * <p>
 * <code>s_0</code> and each <code>r_i</code> is taken from the entropy
 * source. The number of bits in these values should be large enough to
 * ensure that an appreciable amount of entropy is collected. The default
 * constructor will create an object which gets entropy from a PureSpinner
 * object, is seeded with 256 bytes and injects 4 bytes in every round.
 * <p>
 * This RNG is similar to using MD5 in OFB mode, with a secret IV. In each
 * round we have <code>r_i = H(s_0...s_i) = H(s_0...s_{i-1}) +
 * h(H(s_0...s_{i-1}), s_i)</code>, where <code>h</code> is the MD5 round
 * function. We can discard the xor since the left side is known, so the
 * security of the PRNG hinges on the difficulty of predicting a bit of
 * <code>h(x,y)</code> where <code>x</code> is known and of the form given
 * above.
 * <p>
 * The initial seeding may take a while, depending on the entropy source.
 * Therefore the seeding is done in a separate thread which is launched
 * when an object is created. If random bits are requested before the
 * seeding is completed, the request will block.
 *
 * @see org.logi.crypto.random.PureSpinner
 * @version 1.0.6
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 * 
 * Modified 12/2014 Benetech, using SecureRandom instead of Random for better entropy
 */
public class RandomMD5 extends SecureRandom implements Seedable
{

    MD5State.SubState ss = new MD5State.SubState();  // Entropy pool

    /* The number of bytes of seed added since the last hash.
     * It should never exceed 64. */
    int seedSinceHash = 0;

    /* The index in ss.buffer where the next byte of seed should\
     * be xor'd in. */
    int poolSweep = 0;

    /** unused[unusedPos..15] is unused pseudo-random numbers. */
    byte[] unused;
    int unusedPos;

    SecureRandom seeder;        // The Random object to gather enthropy from.
    private int roundEnt; // Bytes to get from seeder

    // Initialization thread which gathers initial entropy
    private Thread initThread;

    private class InitThread extends Thread
    {

        private int seedSize;  // Bytes to get from spinner initially.

        public InitThread(int seedSize)
        {
            this.seedSize = seedSize;
        }

        // Fill entropy pool
        public void run()
        {
            byte[] seed = new byte[seedSize];
            seeder.nextBytes(seed);
            setSeed(seed,0,seedSize);
            initThread=null;
        }

    }

    /**
     * Creates a new instance of the RandomMD5 class. It will be
     * initialized with <code>seedSize</code> bytes from
     * <code>seeder</code> and collects <code>round</code> bytes from
     * it for every 16 bytes it outputs.
     */
    public RandomMD5(SecureRandom seeder, int seedSize, int round)
    {
        this.seeder = seeder;
        roundEnt = round;
        initThread = new InitThread(seedSize);
        initThread.start();
        unused=new byte[16];
        unusedPos=16;
    }

    /**
     * Create a new instance of the RandomMD5 class. It will be
     * initialized with 256 bytes of noise gathered from the
     * scheduler and injects 4 bytes of entropy for each round.
     *
     * @see org.logi.crypto.random.PureSpinner
     */
    public RandomMD5()
    {
         this(new SecureRandom(), 256,4);
    }

    /**
     * Add the bytes from <code>seed[off..off+len-1]</code> to
     * the entropy pool.
     */
    public void setSeed(byte[] seed, int off, int len)
    {
        if(ss==null)
            return;
        if(len+seedSinceHash > 64) {
            // Too much entropy to inject without re-hashing.
            int a=64-seedSinceHash;
            setSeed(seed, off  , a);
            setSeed(seed, off+a, len-a);
        } else {
            // Inject entropy
            for(int i=len-1; i>=0; i--) {
                ss.buffer[poolSweep] ^= seed[off+i];
                poolSweep = (poolSweep+1) % 64;
            }
            seedSinceHash += len;
            if(seedSinceHash>=64) {
                // We need to re-hash.
                ss.transform(ss.buffer,0);
                seedSinceHash=0;
            }
        }
    }

    /**
     * Add the number <code>seed</code> to the entropy pool.
     */
    public void setSeed(long seed)
    {
        byte[] seedBytes = new byte[8];
        Crypto.writeBytes(seed, seedBytes,0,8);
        setSeed(seedBytes,0,8);
    }

    /** Get new unused bytes. */
    private void update()
    {
        // Wait if initialization is not done.
        while(initThread!=null) {
            //System.err.print(".");
            try {
                initThread.join();
            } catch (InterruptedException e) {}
            //System.out.println();
        }

        // Inject entropy into the pool


        byte[] seed = new byte[roundEnt];
        seeder.nextBytes(seed);
        setSeed(seed,0,roundEnt);
        if((seedSinceHash>0) || (roundEnt==0))
            ss.transform(ss.buffer,0);

        // Copy hash to "unused bytes"
        Crypto.writeBytes(ss.hash[0], unused, 0,4);
        Crypto.writeBytes(ss.hash[1], unused, 4,4);
        Crypto.writeBytes(ss.hash[2], unused, 8,4);
        Crypto.writeBytes(ss.hash[3], unused,12,4);
        unusedPos=0;
    }

    /** Generates a user specified number of random bytes. */
    public void nextBytes(byte[] bytes)
    {
        int l=bytes.length;
        int i=0;
        while(i<l) {
            if(unusedPos>=16)
                update();
            int n = Math.min(l-i, 16-unusedPos);
            System.arraycopy(unused,unusedPos, bytes,i, n);
            i+=n;
            unusedPos+=n;
        }
    }

    /** Generates the next random number. */
//    protected synchronized int next(int bits)
//    {
//        int r=0;
//        int b=0;
//        while(b<bits) {
//            if(unusedPos>=16)
//                update();
//            r = (r<<8) | (unused[unusedPos++]&0xff);
//            b+=8;
//        }
//        return r >>> (b-bits);
//    }

}
