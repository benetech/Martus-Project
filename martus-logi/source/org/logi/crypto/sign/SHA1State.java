// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.sign;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.logi.crypto.CryptoCorruptError;
;

/**
 * An object of this class holds the state of a SHA-1 fingerprint still
 * being calculated.
 * <p>
 * This class actually uses java.security.MessageDigest to do all the work.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 *         (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class SHA1State extends HashState
{

    MessageDigest md;

    /** Create a new clear SHA1State. */
    public SHA1State()
    {
        try {
            md=MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoCorruptError("SHA1 algortihm is missing from the java class library.");
        }
    }

    /** The name of the algorithm is "SHA1". */
    public String getName()
    {
        return "SHA1";
    }

    /** Reset the internal state of the object. */
    public void reset()
    {
        md.reset();
    }

    /**
     * Update the hash state with the bytes from
     * <code>buf[offset, offset+length-1]</code>. */
    public void update(byte[] buffer, int offset, int length)
    {
        md.update(buffer,offset,length);
    }

    /**
     * Return a Fingerprint for the curret state, without
     * destroying the state. */
    public Fingerprint calculate()
    {
        try {
            byte[] dig=((MessageDigest)md.clone()).digest();
            return new Fingerprint("SHA1",dig,0,dig.length);
        } catch (CloneNotSupportedException e) {
            throw new CryptoCorruptError("SHA1 algortihm is not cloneable java class library.");
        }
    }

    /**
     * Return the size of input-blocks for this hash function in bytes. */
    public int blockSize()
    {
        return 8;
    }

    /**
     * Returns the size of a fingerprint in bytes. */
    public int hashSize()
    {
        return 20;
    }

}
