package org.logi.crypto.random;

/**
 * This interface will mostly be implemented by random number generators
 * which can inject entropy into an entropy pool. Calling
 * <code>setSeed</code> on any such RNG is guaranteed to not decrease the
 * entropy in the pool.
 *
 * @version 1.0.6
 */
public interface Seedable
{

    /**
     * Add the bytes from <code>seed[off..off+len-1]</code> to
     * the entropy pool.
     */
    public void setSeed(byte[] seed, int off, int len);

    /**
     * Add the number <code>seed</code> to the entropy pool.
     */
    public void setSeed(long seed);
}
