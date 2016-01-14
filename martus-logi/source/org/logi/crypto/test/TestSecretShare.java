// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.test;
import org.logi.crypto.Crypto;
import org.logi.crypto.secretshare.PolySecretShare;
import org.logi.crypto.secretshare.SecretShare;
import org.logi.crypto.secretshare.XorSecretShare;

/**
 * This application tests the secret-sharing classes.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 *
 * @see org.logi.crypto.secretshare.SecretShare
 */
public class TestSecretShare extends Crypto
{

    private TestSecretShare()
    {}

    private static void error()
    {
        System.out.println("Use: java org.logi.crypto.test.TestKey <Xor|Poly>");
    }

    public static void main(String[] arg) throws Exception
    {
        if(arg.length!=1) {
            error();
            return;
        }

        Crypto.initRandom();
        SecretShare[] shares;
        byte[] secret="The Butler did it!".getBytes();

        int n,m;
        if(arg[0].equals("Xor")) {
            m=10;
            n=10;
            shares = XorSecretShare.share(n,secret);
        } else if(arg[0].equals("Poly")) {
            m=7;
            n=10;
            shares = PolySecretShare.share(m,n,secret,512);
        } else {
            error();
            return;
        }

        System.out.println();
        System.out.println("SHARES:");
        for(int i=0; i<shares.length; i++)
            System.out.println(shares[i]);

        System.out.println();
        System.out.println("SECRET: (using first "+m+" shares)");
        byte[] secret2 = SecretShare.retrieve(shares);
        System.out.println(hexString(secret2)+'\t'+new String(secret2));
        System.out.println("Test "+ (equal(secret,secret2)?"passed":"failed"));

        if(m!=n) {
            System.out.println();
            SecretShare[] shares2 = new SecretShare[m];
            for(int i=0; i<m; i++)
                shares2[i]=shares[n-i-1];
            System.out.println("SECRET: (using last "+m+" shares)");
            secret2 = SecretShare.retrieve(shares2);
            System.out.println(hexString(secret2)+'\t'+new String(secret2));
            System.out.println("Test "+ (equal(secret,secret2)?"passed":"failed"));
        }
    }

}
