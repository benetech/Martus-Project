// Copyright (C) 2000 Logi Ragnarsson

package org.logi.crypto.secretshare;
import java.math.BigInteger;

import org.logi.crypto.InvalidCDSException;

/**
 * This class stores a share of a secret.<p>
 
 * The polynomial method can be used for m/n sharing, where n shares are
 * created and m are needed to retrieve the secret.
 
 * The shares s[0..n-1] are points on a polynomial curve which crosses the
 * x-axis
 
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 *         (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class PolySecretShare extends SecretShare
{

    /**
     * Split the secret in n parts such that m are needed to retrieve it. A
     * random prime number will be used for the modulus, at least one bit
     * longer than the secret, but no less than b bits in length. You should
     * make b&gt;=512
     
     * @exception SecretSharingException if the secret can't be m/n-shared.
     */
    public static PolySecretShare[] share(int m, int n, byte[] secret, int b) throws SecretSharingException
    {
        BigInteger p = new BigInteger(Math.max(b,secret.length*8+1),
                                      primeCertainty,
                                      random);
        return share(m, n, p, null, secret);
    }

    /**
     * Split the secret in n parts such that m are needed to retrieve it.
     * Arithmetic will be performed modulus p, which must be longer than the
     * secret.
     *
     * @exception SecretSharingException if the secret can't be m/n-shared.
     */
    public static PolySecretShare[] share(int m, int n, BigInteger p, BigInteger[] x, byte[] secret) throws SecretSharingException
    {
        if(x==null) {
            x=new BigInteger[n];
            for(int i=0; i<n; i++)
                x[i] = BigInteger.valueOf(i+1);
        } else {
            if(x.length!=n)
                throw new SecretSharingException("The number of x-points is not equal to the number of shares");
            for(int i=0; i<n; i++)
                if(x[i].equals(ZERO))
                    throw new  SecretSharingException("A share with x-coordinate 0 is requested. This will geive away the secret.");
        }

        // x is the array of x co-ordinates of shares

        int l = p.bitLength()-1;
        BigInteger[] poly = new BigInteger[m];
        for(int i=1; i<m; i++)
            poly[i] = new BigInteger(l,random);  // top bit zero...
        poly[0]=new BigInteger(1,secret);

        // poly[i] is the i-th coefficient in the sharing polynomial

        PolySecretShare[] r = new PolySecretShare[n];
        for(int i=0; i<n; i++) {
            BigInteger y = poly[0];
            BigInteger xx=x[i];
            for(int j=1; j<poly.length; j++) {
                if(j<poly.length-1)
                    xx = xx.multiply(x[i]).mod(p);
                y = y.add(poly[j].multiply(xx).mod(p));
            }
            y=y.mod(p);

            // y is the polynomial evaluated in x[i]

            r[i] = new PolySecretShare(m, n, x[i], y, p);
        }

        return r;
    }

    /**
     * retrieve the secret from an array of shares.
     *
     * @exception SecretSharingException if the secret can't be retrieved.
     */
    public static byte[] retrieve(SecretShare[] shares) throws SecretSharingException
    {
        int m = shares[0].m;
        if( shares.length < m )
            // We must have at least m shares
            throw new SecretSharingException("Too few shares");

        PolySecretShare[] s = new PolySecretShare[shares.length];
        for(int i=0; i<shares.length; i++) {
            if(! (shares[i] instanceof PolySecretShare))
                throw new SecretSharingException("Share "+i+" is not a PolySecretShare");
            s[i]=(PolySecretShare)shares[i];
        }

        BigInteger p = s[0].p;

        BigInteger L=BigInteger.valueOf(0);
        for(int j=0; j<m; j++) {
            BigInteger a=s[j].y;
            BigInteger b=BigInteger.valueOf(1);
            BigInteger xj = s[j].x;
            for(int i=0; i<m; i++) {
                if(i==j)
                    continue;
                a = a.multiply(s[i].x).mod(p);
                b = b.multiply(xj.subtract(s[i].x)).mod(p);
            }
            L = L.add(a.multiply(b.modInverse(p)));
        }
        L = L.mod(p);
        if(m%2 == 0 && L.bitLength()>0)
            L = p.subtract(L);

        return L.toByteArray();
    }

    BigInteger x,y;   // This share is (x,y) in (Z/pZ)^2
    BigInteger p;

    /** Create an object for an m/n polynomial share.*/
    public PolySecretShare(int m, int n, BigInteger x, BigInteger y, BigInteger p)
    {
        super(m, n);
        this.x=x;
        this.y=y;
        this.p=p;
    }

    /**
     * Used by Crypto.fromString when parsing a CDS.<p>

     * A valid CDS can be created by calling the toString() method.

     * @exception InvalidCDSException if the CDS is malformed.
     * @see org.logi.crypto.Crypto#fromString(String)
     */
    public static PolySecretShare parseCDS(String[] param) throws InvalidCDSException
    {
        if(param.length!=5)
            throw new InvalidCDSException("invalid number of parameters in the CDS PolySecretShare(m,n,x,y,p)");
        int m=Integer.parseInt(param[0]);
        int n=Integer.parseInt(param[1]);
        BigInteger x=new BigInteger(1, fromHexString(param[2]));
        BigInteger y=new BigInteger(1, fromHexString(param[3]));
        BigInteger p=new BigInteger(1, fromHexString(param[4]));
        return new PolySecretShare(m,n,x,y,p);
    }

    /**
     * Return a CDS for this object.
     */
    public String toString()
    {
        StringBuffer sb=new StringBuffer();
        sb.append("PolySecretShare(");
        sb.append(m);
        sb.append(',');
        sb.append(n);
        sb.append(',');
        sb.append(x.toString(16));
        sb.append(',');
        sb.append(y.toString(16));
        sb.append(',');
        sb.append(p.toString(16));
        sb.append(')');
        return sb.toString();
    }

    public int hashCode()
    {
        return x.hashCode() ^ y.hashCode();
    }

    public boolean equals(Object obj)
    {
        if(! (obj instanceof PolySecretShare))
            return false;
        PolySecretShare pss = (PolySecretShare)obj;
        if(n!=pss.n)
            return false;
        if(m!=pss.m)
            return false;
        if(!x.equals(pss.x))
            return false;
        if(!y.equals(pss.y))
            return false;
        if(!p.equals(pss.p))
            return false;
        return true;
    }

}
