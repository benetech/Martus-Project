// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto;

/**
 * This exception is thrown whenever logi.crypto detects that
 * it has been corrupted in some manner. This could be missing classes
 * ot classes that behave in an obiously wrong way.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class CryptoCorruptError extends Error
{

    /** Create a new CryptoCorruptError with no message. */
    public CryptoCorruptError()
    {}

    /** Create a new CryptoCorruptError with the message msg. */
    public CryptoCorruptError(String msg)
    {
        super(msg);
    }

}
