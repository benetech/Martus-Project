// Copyright (C) 2000 Logi Ragnarsson

package org.logi.crypto.secretshare;
import org.logi.crypto.CryptoException;

/**
 * This exception is thrown when there is a problem with secret sharing,
 * such as if there are not enough shares, the shares do not match, etc.
 
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class SecretSharingException extends CryptoException
{

    /** Create a new SecretSharingException with no message. */
    public SecretSharingException()
    {}

    /** Create a new SecretSharingException with the message msg. */
    public SecretSharingException(String msg)
    {
        super(msg);
    }

}
