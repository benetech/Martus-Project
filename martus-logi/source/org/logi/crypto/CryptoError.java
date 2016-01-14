// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto;

import java.lang.Error;

/**
 * This error or its sub-classes are thrown whenever a
 * serious and unforseen cryptographic error occurs.
 *
 * @version 1.0.6
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class CryptoError extends Error
{

    /** Create a new CryptoError with no message. */
    public CryptoError()
    {}

    /** Create a new CryptoError with the message msg. */
    public CryptoError(String msg)
    {
        super(msg);
    }

}
