// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.sign;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.logi.crypto.Crypto;
import org.logi.crypto.CryptoCorruptError;
import org.logi.crypto.InvalidCDSException;

/**
 * An subclasses of this object handle the status of a fingerprint still
 * being calculated.
 * <p>
 * Instances can be continually updated with data and at any point a
 * Fingerprint for the data added so far can be requested.
 * 
 * @see org.logi.crypto.sign.Fingerprint
 * @version 1.0.6
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a> (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public abstract class HashState extends Crypto
{

    // FINGERPRINT CLASS LIBRARIAN

    static private String defaultHashFunction="SHA1";

    /**
     * Create a HashState object for the named hash function.
     *
     * @exception InvalidCDSException if a HashState object for the
     *            named algorithm could not be created.
     */
    public static HashState create(String algorithm) throws InvalidCDSException
    {
        // Construct an object
        Class cl=Crypto.makeClass(algorithm+"State");

        Constructor con;
        try {
            Class[] parType = new Class[0];
            con=cl.getConstructor(parType);
        } catch (Exception e) {
            throw new InvalidCDSException(algorithm+" does not have a "+algorithm+"State() constructor");
        }

        Object r;
        try {
            Object[] arg = new Class[0];
            r = con.newInstance(arg);
        } catch (InvocationTargetException e1) {
            throw new InvalidCDSException("Unable to create an instance of "+algorithm+"State [ "+e1.getTargetException().toString()+" ]");
        }
        catch (Exception e) {
            throw new InvalidCDSException("Unable to create an instance of "+algorithm);
        }
        try {
            return (HashState) r;
        } catch (ClassCastException e) {
            throw new InvalidCDSException(algorithm+"State is not a descendant of HashState");
        }
    }

    /**
     * Create a HashState object for the default hash function.
     */
    public static HashState create()
    {
        try {
            return create(defaultHashFunction);
        } catch (InvalidCDSException e) {
            throw new CryptoCorruptError("The HashState object for "+defaultHashFunction+" has disappeared.");
        }
    }

    /**
     * Sets the default hash-function. It must be the name of a supported
     * hash function, such as SHA1 or MD5.
     *
     * <p>Note that changing this will invalidate all values previously
     * returned by the hash() method of objects which generate the hash
     * from a cryptographic fingerprint of themselves. This call should
     * preferably be made before any such hash vlaues are returned, f.ex.
     * in KeyRing objects.
     *
     * @exception InvalidCDSException if a HashState object for the
     *            named algorithm could not be created.
     */
    public static void setDefaultHashFunction(String algorithm) throws InvalidCDSException
    {
        create(algorithm);  // to get an exception is it fails.
        defaultHashFunction = algorithm;
    }

    /**
     * Returns the default hash-function.     */
    public static String getDefaultHashFunction()
    {
        return defaultHashFunction;
    }

    // INSTANCE METHODS

    /** Return the name of the algorithm used by this HashState object. */
    public abstract String getName();

    /** Reset the state. */
    public abstract void reset();

    /**
     * Update the hash state with the bytes from
     * <code>buf[offset, offset+length-1]</code>. */
    public abstract void update(byte[] buf, int offset, int length);

    /** Update the hash state with the bytes from <code>buf</code>. */
    public void update(byte[] buf)
    {
        update(buf, 0, buf.length);
    }

    /** Update the hash state with the characters from <code>s</code>. */
    public void update(String s)
    {
        update(s.getBytes());
    }

    /**
     * Return a fingerprint for the curret state, without
     * destroying the state. */
    public abstract Fingerprint calculate();

    /**
     * Return the size of input-blocks for this hash function in bytes. */
    public abstract int blockSize();

    /**
     * Returns the size of a fingerprint in bytes. */
    public abstract int hashSize();
}
