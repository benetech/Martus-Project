// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto.keys;


/**
 * This interface is implemented by keys which handle decryption of single
 * blocks of data.
 *
 * @author <a href="http://www.logi.org/">Logi Ragnarsson</a> (<a href="mailto:logi@logi.org">logi@logi.org</a>)
 */
public interface DecryptionKey
            extends Key
{

    /**
     * Returns the size of the blocks that can be encrypted in one call
     * to encrypt(). */
    public int plainBlockSize();

    /**
     * Returns the size of the blocks that can be decrypted in one call
     * to decrypt(). */
    public int cipherBlockSize();

    /**
     * Decrypt one block of data. The ciphertext is taken from
     * <code>source</code> starting at offset <code>i</code> and
     * plaintext is written to <code>dest</code>, starting at
     * offset <code>j</code>.
     * <p>
     * The amount of data read and written will match the values returned
     * by <code>cipherBlockSize()</code> and <code>plainBlockSize()</code>.
     */
    public void decrypt(byte[] source, int i, byte[] dest, int j);

}
