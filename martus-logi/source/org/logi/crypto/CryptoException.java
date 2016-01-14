// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto;


/**
 * This exception or its sub-classes are thrown
 * whenever a cryptographic error occurs.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a> (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class CryptoException extends Exception
{

    /** Create a new CryptoException with no message. */
    public CryptoException()
    {}

    /** Create a new CryptoException with the message msg. */
    public CryptoException(String msg)
    {
        super(msg);
    }

}
