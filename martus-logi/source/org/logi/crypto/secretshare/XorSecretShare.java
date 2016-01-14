// Copyright (C) 2000 Logi Ragnarsson

package org.logi.crypto.secretshare;
import org.logi.crypto.*;

/**
 * This class stores a share of a secret.<p>
 
 * The xor method can only be used for n/n sharing, where n shares are
 * created and all are needed to retrieve the secret.
 
 * The shares s[0..n-2] are simply random numbers, while s[n] is the
 * exclusive or of all the other shares along with the secret.
 
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 *         (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class XorSecretShare extends SecretShare
{

    protected byte[] share;  // The actual share.

    /**
     Split the secret in n parts. All are needed to retrieve it. */
    public static XorSecretShare[] share(int n, byte[] secret)
    {
        int l=secret.length;
        byte[] last = new byte[l];
        System.arraycopy(secret,0, last,0, l);

        XorSecretShare[] r = new XorSecretShare[n];
        for(int i=0; i<n-1; i++) {
            byte[] s = new byte[l];
            random.nextBytes(s);
            for(int j=0; j<l; j++)
                last[j] ^= s[j];
            r[i] = new XorSecretShare(n,s,false);
        }
        r[n-1] = new XorSecretShare(n,last,false);
        return r;
    }

    /**
     * retrieve the secret from an array of shares.

     * @exception SecretSharingException if the secret can't be retrieved. */
    public static byte[] retrieve(SecretShare[] shares) throws SecretSharingException
    {
        int m = shares[0].m;
        if( shares.length < m )
            // We must have at least m shares
            throw new SecretSharingException("Too few shares");

        if(! (shares[0] instanceof XorSecretShare))
            throw new SecretSharingException("Share 0 is not an XorSecretShare");
        byte[] s = ((XorSecretShare)shares[0]).share;
        int l = s.length;
        byte[] r = new byte[l];
        System.arraycopy(s,0, r,0, l);

        for(int i=1; i<m; i++) {
            if(! (shares[i] instanceof XorSecretShare))
                throw new SecretSharingException("Share "+i+" is not an XorSecretShare");
            s = ((XorSecretShare)shares[i]).share;
            if(s.length != l)
                throw new SecretSharingException("Share "+i+" is not of the same length as share 0");
            for(int j=0; j<l; j++)
                r[j] ^= s[j];
        }
        return r;
    }

    /** Create an object for an n/n xor-share.*/
    public XorSecretShare(int n, byte[] share)
    {
        this(n, share, true);
    }

    /**
     * Create an object for an n/n xor-share. If copy is true, then the
     * content of the array is copied to a new array. Otherwise only the
     * reference is copied and tme content should not be changed by the
     * caller. */
    public XorSecretShare(int n, byte[] share, boolean copy)
    {
        super(n,n);
        if(copy) {
            this.share = new byte[share.length];
            System.arraycopy(share,0, this.share,0, share.length);
        } else
            this.share=share;
    }

    /** Get the actual bytes of the share. */
    public byte[] getShare()
    {
        return share;
    }

    /**
     * Used by Crypto.fromString when parsing a CDS.<p>

     * A valid CDS can be created by calling the toString() method.

     * @exception InvalidCDSException if the CDS is malformed.
     * @see org.logi.crypto.Crypto#fromString(String)
     */
    public static XorSecretShare parseCDS(String[] param) throws InvalidCDSException
    {
        if(param.length!=2)
            throw new InvalidCDSException("invalid number of parameters in the CDS XorSecretShare(n,share)");
        int m = Integer.parseInt(param[0]);
        byte[] share=fromHexString(param[1]);
        return new XorSecretShare(m, share, false);
    }

    /**
     * Return a CDS for this object.
     */
    public String toString()
    {
        StringBuffer sb=new StringBuffer();
        sb.append("XorSecretShare(");
        sb.append(n);
        sb.append(',');
        sb.append(hexString(share));
        sb.append(')');
        return sb.toString();
    }

    public int hashCode()
    {
        int h=0;
        for (int i=0; i<share.length; i++)
            h = h ^ ( share[i] << (i%4)*8 );
        return h;
    }

    public boolean equals(Object obj)
    {
        if(! (obj instanceof XorSecretShare))
            return false;
        XorSecretShare xss = (XorSecretShare)obj;
        if(n!=xss.n)
            return false;
        if(m!=xss.m)
            return false;
        if(!equal(share, xss.share))
            return false;
        return true;
    }

}
