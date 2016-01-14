// Copyright (C) 2000 Logi Ragnarsson

package org.logi.crypto.sign;
import java.io.IOException;
import java.io.PrintWriter;

import org.logi.crypto.Crypto;
import org.logi.crypto.InvalidCDSException;
/**
 * This class is used to hold a fingerprint of a particular data buffer.
 * <p>
 * The idea is to calculate a fingerprint in such a way that it is
 * difficult to create a buffer that gives a particular fingerprint. If
 * that buffer also has to match some other criteria, such as being a valid 
 * text file in a particular language, then it becomes next to impossible.
 * <p>
 * All this depends on the hash function used to create the fingerprint
 * being a good one. Fingerprints are created by the various subclasses
 * of HashState, so you should look there for information about
 * a particular hash function.
 * <p>
 * The CDS for a Fingerprint object is <code>Fingerprint(name,fp)</code>
 * where <code>name</code> is the name of the hash function used and
 * <code>fp</code> the actual fingerprint.
 *
 * @see org.logi.crypto.sign.HashState
 * @see org.logi.crypto.sign.Signature
 * @version 1.1.0
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a> (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class Fingerprint extends Crypto
{

    // CREATE FINGERPRINTS DIRECTLY

    /**
     * Create a Fingerprint object. It will contain a fingerprint for the
     * data in <code>buf[offset..offset+length-1]</code> calculated with
     * the named fingerprint hash function.
     *
     * @exception InvalidCDSException if a HashState object for the
     *            named hash function could not be created.
     */
    public static Fingerprint create(byte[] buf, int offset, int length, String hashFunc) throws InvalidCDSException
    {
        HashState fps = HashState.create(hashFunc);
        fps.update(buf, offset, length);
        return fps.calculate();
    }

    /**
     * Create a Fingerprint object. It will contain a fingerprint for the
     * data in <code>buf</code> calculated with the named
     * fingerprint hash function.
     *
     * @exception InvalidCDSException if a HashState object for the
     *            named hash function could not be created.
     */
    public static Fingerprint create(byte[] buf, String hashFunc) throws InvalidCDSException
    {
        return Fingerprint.create(buf, 0, buf.length, hashFunc);
    }

    /**
     * Create a Fingerprint object. It will contain a fingerprint for the
     * string s calculated with the named fingerprint hash function.
     *
     * @exception InvalidCDSException if a HashState object for the
     *            named hash function could not be created.
     */
    public static Fingerprint create(String s, String hashFunc) throws InvalidCDSException
    {
        HashState fps = HashState.create(hashFunc);
        fps.update(s);
        return fps.calculate();
    }

    // =================================================================== //

    /** Holds the actual bytes of the fingerprint value. */
    protected byte[]fp=null;

    /** Holds the name of the hash function used to create this fingerprint. */
    protected String hashFunc=null;

    /**
     * Creates a new Fingerprint object. It contains the hash value from
     * <code>fp[offset..offset+n-1]</code> which was generated with the
     * named hash function. */
    public Fingerprint(String hashFunc, byte[] fp, int offset, int n)
    {
        this.hashFunc = hashFunc;
        this.fp = new byte[n];
        System.arraycopy(fp,offset, this.fp,0, n);
    }

    /**
     * Creates a new Fingerprint object. It contains the hash value from
     * <code>fp</code> which was generated with the named hash function. */
    public Fingerprint(String hashFunc, byte[] fp)
    {
        this(hashFunc, fp, 0, fp.length);
    }

    /**
     * Used by Crypto.fromString when parsing a CDS.<p>

     * A valid CDS can be created by calling the toString() method.

     * @exception InvalidCDSException if the CDS is malformed.
     * @see org.logi.crypto.Crypto#fromString(String)
     */
    public static Fingerprint parseCDS(String[] param) throws InvalidCDSException
    {
        if(param.length!=2)
            throw new InvalidCDSException("invalid number of parameters in the CDS Fingerprint(hashFunc,fingerprint)");
        return new Fingerprint(param[0], fromHexString(param[1]));
    }

    /** Return the name of the hash function used for this fingerprint. */
    public String getHashFunc()
    {
        return hashFunc;
    }

    /** Return an array of the bytes in the fingerprint. */
    public byte[] getBytes()
    {
        byte[] fp=new byte[this.fp.length];
        System.arraycopy(this.fp,0, fp,0, this.fp.length);
        return fp;
    }

    /**
     * Test for equality with another object. Returns true if
     * <code>obj</code> is a Fingerprint equal to <code>this</code>. */
    public boolean equals(Object obj)
    {
        if (obj==null)
            return false;
        if (! (obj instanceof Fingerprint))
            return false;
        // It's the same class

        Fingerprint f=(Fingerprint)obj;
        if (!f.hashFunc.equals(hashFunc))
            return false;
        // It's the same hash function.

        for (int i=fp.length-1; i>=0; i--)
            // this.fp[j] == fp[j] for j=0, 1, ..., i-1.
            if (fp[i]!=f.fp[i])
                return false;

        return true;
    }

    /**
     * Return a hash-code based on the bytes of the
     * fingerprint and the hash function name.
     */
    public int hashCode()
    {
        int h=hashFunc.hashCode();
        for (int i=0; i<fp.length; i++)
            h = h ^ ( fp[i] << (i%4)*8 );
        return h;
    }

    /**
     * Return a CDS for this fingerprint.
     */
    public String toString()
    {
        return "Fingerprint("+hashFunc+","+hexString(fp)+")";
    }

    /**
     * Print this object to out, indented with ind tabs, going down at most
     * rec levels of recursion. */
    public void prettyPrint(PrintWriter out, int ind, int rec) throws IOException
    {
        if(rec<0)
            return;
        for(int i=0; i<ind; i++)
            out.print('\t');
        out.println("Fingerprint(");

        for(int i=0; i<=ind; i++)
            out.print('\t');
        out.print(hashFunc==null ? "null" : hashFunc);
        out.println(",");

        for(int i=0; i<=ind; i++)
            out.print('\t');
        out.print(fp==null ? "null" : hexString(fp));
        out.println();

        for(int i=0; i<ind; i++)
            out.print('\t');
        out.print(")");
    }

}
