// Copyright (C) 1998 Logi Ragnarsson

package org.logi.crypto.keys;


/**
 * This interface is implemented by keys which handle encryption of single
 * blocks of data.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a> (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public interface EncryptionKey
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
     * Encrypt one block of data. The plaintext is taken from
     * <code>source</code> starting at offset <code>i</code> and
     * ciphertext is written to <code>dest</code>, starting at
     * offset <code>j</code>.
     * <p>
     * The amount of data read and written will match the values returned
     * by <code>plainBlockSize()</code> and <code>cipherBlockSize()</code>.
     */
    public void encrypt(byte[] source, int i, byte[] dest, int j);

}
