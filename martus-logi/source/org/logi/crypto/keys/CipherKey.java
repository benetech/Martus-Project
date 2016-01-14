// Copyright (C) 1998-2001 Logi Ragnarsson

package org.logi.crypto.keys;


/**
 * This interface is implemented by keys which handle encryption and
 * decryption of single blocks of data.
 *
 * @author <a href="http://www.logi.org/">Logi Ragnarsson</a> (<a href="mailto:logi@logi.org">logi@logi.org</a>)
 */
public interface CipherKey
            extends EncryptionKey, DecryptionKey
    {}
