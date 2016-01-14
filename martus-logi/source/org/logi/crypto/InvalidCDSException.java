// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto;


/**
 * This exception is thrown whenever a malformed CDS is encountered.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a> (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class InvalidCDSException extends CryptoException
{

    /** Create a new InvalidCDSException with no message. */
    public InvalidCDSException()
    {}

    /** Create a new InvalidCDSException with the message msg. */
    public InvalidCDSException(String msg)
    {
        super(msg);
    }

}
