// Copyright (C) 2000-2001 Logi Ragnarsson

package org.logi.crypto.secretshare;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.logi.crypto.Crypto;

/**
 * This class stores a share of a secret.<p>

 * If a secret is m/n-shared this means that n shares are created and that
 * M shares are needed to retrieve the secret.<p>

 * If the sharing algorithm is secure, then m-1 shares gives no information
 * about the secret.

 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 *         (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public abstract class SecretShare
    extends Crypto
{

    protected int m,n;  // This is an m/n share.

    /**
     * Create an object for an m/n share. */
    public SecretShare(int m, int n)
    {
        this.m=m;
        this.n=n;
    }

    /** Get the number of created shares. */
    public int getN()
    {
        return n;
    }

    /** Get the number of shares needed to retrieve the secret. */
    public int getM()
    {
        return m;
    }

    /**
     * retrieve the secret from an array of shares.

     * @exception SecretSharingException if the secret can't be retrieved.
     */
    public static byte[] retrieve(SecretShare[] shares) throws SecretSharingException
    {
        Class cl = shares[0].getClass();
        Method retrieve;
        try {
            SecretShare[] s = new SecretShare[0];  // FIXME: UGLY!!
            Class[] parType = { s.getClass() };
            retrieve=cl.getMethod("retrieve", parType);
        } catch (Exception e) {
            throw new SecretSharingException(cl+" does not have a proper retrieve method.");
        }

        Object r;
        try {
            Object[] arg = { shares };
            r = retrieve.invoke(null,arg);
        } catch (InvocationTargetException e1) {
            Throwable e2=e1.getTargetException();
            if (e2 instanceof SecretSharingException)
                throw (SecretSharingException)e2;
            throw new SecretSharingException("Strange exception in retrieve method: "+e2.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            throw new SecretSharingException("Error retrieving secret");
        }
        return (byte[])r;

    }


}
