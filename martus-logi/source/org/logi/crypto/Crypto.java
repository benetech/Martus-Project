// Copyright (C) 1998-2001 Logi Ragnarsson

package org.logi.crypto;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Random;

import org.logi.crypto.keys.CipherKey;
import org.logi.crypto.keys.KeySource;
import org.logi.crypto.random.RandomFromStream;
import org.logi.crypto.random.RandomMD5;
import org.logi.crypto.sign.Fingerprint;


/**
 * This class contains numerous static and final utility functions
 * along with global variables for the logi.crypto package.
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 * (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public abstract class Crypto
{

    // -----------------------------------------------------------
    // CLASS VARIABLES

    private static class WarnRandom
        extends Random
    {
        public WarnRandom()
        { }

        protected synchronized int next(int bits)
        {
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
        }

        // NOTE: Override ALL get methods, to ensure this class is never actually used
        // This change was made by Benetech
		public void nextBytes(byte[] bytes) 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public int nextInt() 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public int nextInt(int n) 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public long nextLong() 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public boolean nextBoolean() 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public float nextFloat() 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public double nextDouble() 
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}

		public synchronized double nextGaussian()
		{
            throw new CryptoError("A org.logi.crypto.Crypto.initRandom(...) method has not been called.");
		}
    }


    /**
     * This is the default random generator used by various Crypto
     * classes. It should be a cryptographically secure
     * random number generator, preferably without any period, which
     * rules out all generators based on iterated functions, such as
     * java.util.Random.
     */
    public static Random random = new WarnRandom();

    /**
     * We allow a chance of 0.5**primeCertainty chance that given a composite
     * number, the primaility check will say it is a prime. For complicated
     * reasons, the actual chance of generating a false prime is <i>much</i>
     * lower.
     */
    public static int primeCertainty=20;

    /** The constant zero. */
    public static final BigInteger ZERO=BigInteger.valueOf(0);

    /** The constant one. */
    public static final BigInteger ONE=BigInteger.valueOf(1);

    /** The constant two. */
    public static final BigInteger TWO=BigInteger.valueOf(2);

    /** The constant four. */
    public static final BigInteger FOUR=BigInteger.valueOf(4);

    /** An empty byte array. */
    public static final byte[] EMPTY_ARRAY=new byte[0];

    /**
     * The object used to store and retrieve keys.
     * It is used by the <code>lookup(fingerprint)</code> CDS.
     */
    public static KeySource keySource;



    // -----------------------------------------------------------
    // INITIALIZATION


    /**
     * Initialize the logi.crypto library. One of the initRandom methods
     * must be called before the library is asked to do anything requiering
     * random numbers.
     * <p>
     * The random number generator used by various parts of the logi.crypto
     * library will be set to be the object specified in the
     * <code>r</code> parameter.
     *
     * @see org.logi.crypto.random.RandomMD5
     * @see org.logi.crypto.random.RandomFromStream
     */
    public static void initRandom(Random r)
    {
        if(r==null)
            throw new NullPointerException();
        random = r;
    }


    /**
     * Initialize the logi.crypto library. One of the initRandom methods
     * must be called before the library is asked to do anything requiering
     * random numbers.
     * <p>
     * This method will not do anything if one of the initRandom methods has
     * been called previously.
     * <p>
     * The default random number generator will be used.  If the file
     * <code>/dev/urandom</code> can be read, this is an instance of the
     * <code>RandomFromStream</code> class which reads that file.
     * Otherwise an instance of the <code>RandomMD5</code> class is used.
     *
     * @see org.logi.crypto.random.RandomMD5
     * @see org.logi.crypto.random.RandomFromStream
     */
    public static void initRandom()
    {
        if(!(random instanceof WarnRandom))
            return;
        Random r;
        try {
            // Try to use random bits from the random number device
            r = new RandomFromStream(new FileInputStream("/dev/urandom"));
        } catch (Throwable e) {
            // /dev/random does not exists or is not readable
            // (may be security exception if we are an applet)
            r = new RandomMD5();
        }
        initRandom(r);
    }



    //////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS


    /**
     * Convert a byte array to a long. Bits are collected from
     * <code>buf[i..i+length-1]</code>. */
    public static final long makeLong(byte[] buf, int i, int length)
    {
        long r=0;
        length+=i;
        for (int j=i; j<length; j++)
            r= (r<<8) | (buf[j] & 0xffL);
        return r;
    }


    /**
     * Convert a byte array to an int. Bits are collected from
     * <code>buf[i..i+length-1]</code>. */
    public static final int makeInt(byte[] buf, int i, int length)
    {
        int r=0;
        length+=i;
        for (int j=i; j<length; j++)
            r= (r<<8) | (buf[j] & 0xff);
        return r;
    }


    /**
     * Write a long to a byte array. Bits from <code>a</code> are written
     * to <code>dest[i..i+length-1]</code>. */
    public static final void writeBytes(long a, byte[] dest, int i, int length)
    {
        for (int j=i+length-1; j>=i; j--) {
            dest[j]=(byte)a;
            a = a >>> 8;
        }
    }


    /**
     * Write an int to a byte array. Bits from <code>a</code> are written
     * to <code>dest[i..i+length-1]</code>. */
    public static final void writeBytes(int a, byte[] dest, int i, int length)
    {
        for (int j=i+length-1; j>=i; j--) {
            dest[j]=(byte)a;
            a = a >>> 8;
        }
    }


    /**
     * Construct an int by picking bits from another int. The number in
     * <code>bits[i]</code> is the index of the bit within <code>a</code>
     * that should be put at index <code>i</code> in the result.
     * <p>
     * The most-significant bit is number 0.
     */
    public static final int pickBits(int a, byte[] bits)
    {
        int r=0;
        int l=bits.length;
        for (int b=0; b<l; b++)
            r = (r<<1) | ((a >>> (31-bits[b])) & 1);
        return r;
    }


    /**
     * Construct an long by picking bits from another long. The number in
     * <code>bits[i]</code> is the index of the bit within <code>a</code>
     * that should be put at index <code>i</code> in the result.
     * <p>
     * The most-significant bit is number 0.
     */
    public static final long pickBits(long a, byte[] bits)
    {
        long r=0;
        int l=bits.length;
        for (int b=0; b<l; b++)
            r = (r<<1) | ((a >>> (63-bits[b])) & 1);
        return r;
    }


    /** The hexadecimal digits "0" through "f". */
    public static char[] NIBBLE = {
                                      '0', '1', '2', '3', '4', '5', '6', '7',
                                      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
                                  };


    /**
     * Convert a byte array to a string of hexadecimal digits.
     */
    public static final String hexString(byte[] buf)
    {
        return hexString(buf, 0, buf.length);
    }


    /**
     * Convert a byte array to a string of hexadecimal digits.
     * The bytes <code>buf[i..i+length-1]</code> are used.
     */
    public static final String hexString(byte[] buf, int i, int length)
    {
        StringBuffer sb = new StringBuffer(length*2);
        for (int j=i; j<i+length; j++) {
            sb.append(NIBBLE[(buf[j]>>>4)&15]);
            sb.append(NIBBLE[ buf[j]     &15]);
        }
        return sb.toString();
    }


    /**
     * Convert a long to a string of hexadecimal digits.
     */
    public static final String hexString(long a)
    {
        StringBuffer sb = new StringBuffer(16);
        for (int i=0; i<16; i++)
            sb.append(NIBBLE[(int)(a >>> (60-4*i)) & 0xf]);
        return sb.toString();
    }


    /**
     * Convert an int to a string of hexadecimal digits.
     */
    public static final String hexString(int a)
    {
        StringBuffer sb = new StringBuffer(8);
        for (int i=0; i<8; i++)
            sb.append(NIBBLE[(int)(a >>> (60-4*i)) & 0xf]);
        return sb.toString();
    }


    /**
     * Convert a byte to a string of hexadecimal digits.
     */
    public static final String hexString(byte a)
    {
        StringBuffer sb = new StringBuffer(2);
        sb.append(NIBBLE[(a>>>4)&0xf]);
        sb.append(NIBBLE[a&0xf]);
        return sb.toString();
    }


    /**
     * Convert a hexadecimal digit to a byte.
     */
    public static byte fromHexNibble(char n)
    {
        if(n<='9')
            return (byte)(n-'0');
        if(n<='G')
            return (byte)(n-('A'-10));
        return (byte)(n-('a'-10));
    }


    /**
     * Convert a string of hexadecimal digits to a byte array.
     */
    public static byte[] fromHexString(String hex)
    {
        int l=(hex.length()+1)/2;
        byte[] r = new byte[l];
        int i = 0;
        int j = 0;
        if(hex.length()%2 == 1) {
            // Odd number of characters: must handle half byte first.
            r[0]=fromHexNibble(hex.charAt(0));
            i=j=1;
        }
        while(i<l)
            r[i++] = (byte)((fromHexNibble(hex.charAt(j++)) << 4) | fromHexNibble(hex.charAt(j++)));
        return r;
    }


    /** The binary digits "0" and "1". */
    public static char[] BIT = { '0', '1' };


    /**
     * Convert a long to a string of binary digits.
     */
    public static final String binString(long a)
    {
        StringBuffer sb = new StringBuffer(64);
        for (int i=0; i<64; i++)
            sb.append(BIT[(int)(a >>> (63-i)) & 0x1]);
        return sb.toString();
    }


    /**
     * Convert an int to a string of binary digits.
     */
    public static final String binString(int a)
    {
        StringBuffer sb = new StringBuffer(32);
        for (int i=0; i<32; i++)
            sb.append(BIT[(int)(a >>> (31-i)) & 0x1]);
        return sb.toString();
    }


    /**
     * Return true iff two array contain the same bytes.
     */
    public static boolean equal(byte[] a, byte[] b)
    {
        if(a.length!=b.length)
            return false;
        for(int i=a.length-1; i>=0; i--)
            if(a[i]!=b[i])
                return false;
        return true;
    }


    /**
     * Return true iff two arrays contain the same bytes, discounting
     * any zero bytes from the front of the arrays.
     */
    public static boolean equalRelaxed(byte[] a, byte[] b)
    {
        // I assume the compiler knows that the various length fields are
        // constant. (I should look at the bytecode, but can't be bothered)
        if(a.length>b.length) {
            for(int i=a.length-b.length-1; i>=0; i--)
                if(a[i]!=0)
                    return false;
            for(int i=a.length-1; i>a.length-b.length; i--)
                if(a[i]!=b[i+b.length-a.length])
                    return false;
        } else {
            for(int i=b.length-a.length-1; i>=0; i--)
                if(b[i]!=0)
                    return false;
            for(int i=b.length-1; i>b.length-a.length; i--)
                if(b[i]!=a[i+a.length-b.length])
                    return false;
        }
        return true;
    }


    /**
     * Return true iff a sub-array of two arrays contain the same bytes.
     * Compares <code>a[i..i+length-1]</code> and <code>b[j..j+length-1]</code>.
     */
    public static boolean equalSub(byte[] a, int i, byte[] b, int j, int length)
    {
        int end=i+length;
        while(i<end)
            if (a[i++] != b[j++])
                return false;
        return true;
    }


    /**
     * Either returns a or a new array contianing the first i bytes from a.
     * The returned array is ensured to be at least l bytes long. */
    public static final byte[] ensureArrayLength(byte[] a, int i, int l)
    {
        if(a.length>=l)
            return a;
        byte[] r = new byte[Math.max(2*a.length,l)];
        System.arraycopy(a,0, r,0, i);
        return r;
    }


    /**
     * Either returns a or a new array contianing the first i bytes from a.
     * The returned array will be i bytes long. */
    public static final byte[] trimArrayLength(byte[] a, int i)
    {
        if(a.length==i)
            return a;
        byte[] r = new byte[i];
        System.arraycopy(a,0, r,0, i);
        return r;
    }


    /**
     * Either returns a or a new array contianing the first i strings from a.
     * The returned array is ensured to be at least l bytes long. */
    public static final String[] ensureArrayLength(String[] a, int i, int l)
    {
        if(a.length>=l)
            return a;
        String[] r = new String[Math.max(2*a.length,l)];
        System.arraycopy(a,0, r,0, i);
        return r;
    }


    /**
     * Either returns a or a new array contianing the first i Stringd from a.
     * The returned array will be i bytes long. */
    public static final String[] trimArrayLength(String[] a, int i)
    {
        if(a.length==i)
            return a;
        String[] r = new String[i];
        System.arraycopy(a,0, r,0, i);
        return r;
    }


    /**
     * Either returns a or a new array. The returned array will contain
     * exactly the same bytes as a, except for leading zeroes. */
    public static final byte[] trimLeadingZeroes(byte[] a)
    {
        if(a[0]!=0)
            return a;
        int n=1;
        int N=a.length;
        while(n<N && a[n]==0)
            n++;
        byte[] r = new byte[N-n];
        System.arraycopy(a,n, r,0, N-n);
        return r;
    }


    /**
     * Write an int to an OutputStream in bigendian order. */
    public static final void writeInt(OutputStream out, int x) throws IOException
    {
        out.write(x >>> 24);
        out.write(x >>> 16);
        out.write(x >>>  8);
        out.write(x       );
    }


    /**
     * Read an int from an InputStream in bigendian order. */
    public static final int readInt(InputStream in) throws IOException
    {
        int a,b,c,d;
        a = in.read();
        b = in.read();
        c = in.read();
        d = in.read();
        if( (a|b|c|d) < 0)
            throw new EOFException();
        return (a<<24) | (b<<16) | (c<<8) | d;
    }


    /**
     * Reads a number of bytes, blocking until they are all available.
     * Returns -1 if EOF is reached before reading len bytes, otherwise
     * returns len. Bytes are put in buf, starting at index i. */
    public static final int readBlock(InputStream in, byte[] buf, int i, int len) throws IOException
    {
        int left=len;
        while(left>0) {
            int n = in.read(buf,i,left);
            if(n==-1)
                return -1;
            i+=n;
            left-=n;
        }
        return len;
    }



    //////////////////////////////////////////////////////////////////////////
    // CDS PARSING METHODS


    /**
     * The array of names of packages that are searched for classes
     * mentioned in a CDS.
     */
    public static String[] cdsPath = {
                                         "org.logi.crypto.",
                                         "org.logi.crypto.keys.",
                                         "org.logi.crypto.sign.",
                                         "org.logi.crypto.secretshare."
                                     };


    /**
     * Create a Class object for the named class. The class is searched
     * for in the packages named in the CDS Path, which by default
     * includes the appropriate logi.crypto package names.
     *
     * @exception InvalidCDSException if the class could not be created
     */
    public static Class makeClass(String name) throws InvalidCDSException
    {
        for(int i=0; i<cdsPath.length; i++) {
            try {
                return Class.forName(cdsPath[i]+name);
            } catch (ClassNotFoundException e) { }
        }
        throw new InvalidCDSException("The class "+name+" was not found");
    }


    /**
     * Convert a byte array to a CipherKey. Returns a new key of type
     * <code>keyType</code>, with key-material from <code>bits</code>.
     * <p>
     * <code>keyType</code> should be the name of a class which implements
     * the CipherKey interface, such as "TriDESKey".
     *
     * @exception InvalidCDSException if the key could not be created
     */
    public static CipherKey makeSessionKey(String keyType, byte[] bits) throws InvalidCDSException
    {
        // Construct an object
        Class cl=makeClass(keyType);

        Constructor con;
        try {
            Class[] parType = { bits.getClass() };
            con=cl.getConstructor(parType);
        } catch (Exception e) {
            throw new InvalidCDSException(keyType+" does not have a parseCDS(byte[]) constructor");
        }

        Object r;
        try {
            Object[] arg = { bits };
            r = con.newInstance(arg);
        } catch (InvocationTargetException e1) {
            throw new InvalidCDSException("Unable to create an instance of "+keyType+" [ "+e1.getTargetException().toString()+" ]");
        }
        catch (Exception e) {
            throw new InvalidCDSException("Unable to create an instance of "+keyType);
        }

        try {
            return (CipherKey)r;
        } catch(ClassCastException e) {
            throw new InvalidCDSException(keyType+" does not implement CipherKey");
        }
    }


    /**
     * Read characters from a Reader until a non-space character
     * is reached and return that character. */
    public static int pastSpace(Reader r) throws IOException
    {
        int ch=' ';
        while(Character.isWhitespace((char)ch) && (ch!=-1))
            ch=r.read();
        return ch;
    }


    /* B: a class-name followed by '(' has just been read from cds.
     * A: the "," delmited list of parameters from cds is in the result and
     *    a ')' has just been read.
     */
    private static String[] splitParams(Reader cds) throws IOException, InvalidCDSException
    {
        String[] r=new String[2];
        int n=0;

        // r[0..n-1] is the parameters read so far.

        int paramNest=0;
        boolean quoted=false;

        StringBuffer sb=new StringBuffer();

        int ch=cds.read();
        while(true) {
            if(ch==-1)
                throw new InvalidCDSException("Parameter list not closed with \")\"");
            boolean end=(ch==')' && (!quoted) && paramNest==0);
            if((ch==',' && (!quoted)) && paramNest==0 || end) {
                // end of a parameter
                if(n==r.length) {
                    String[] t=new String[2*n];
                    System.arraycopy(r,0, t,0, n);
                    r=t;
                }
                r[n++ ]=sb.toString().trim();
                sb.setLength(0);
            } else if(ch=='"') {
                // quoting
                quoted=!quoted;
            } else {
                // part of an actual parameter
                sb.append((char)ch);
                if(ch==')' && (!quoted))
                    paramNest--;
                if(ch=='(' && (!quoted))
                    paramNest++;
            }

            if(end)
                break;
            ch=cds.read();
        }

        if(n==r.length)
            // r is exactly full.
            return r;

        // r is too long, so we trim it and return
        String[] t=new String[n];
        System.arraycopy(r,0, t,0, n);
        return t;
    }


    /**
     * Parse the given Cipher Description String (CDS).
     * <p>
     * This method can be used to parse a CDS such as that returned by
     * the Key and Fingerprint <code>toString()</code> methods and return the
     * described object.
     * <p>
     * The CDS syntax is one of:<ul>
     *   <li><code>ClassName(parameters)</code>
     *   <li><code>lookup(fingerprint)</code>
     * </ul>
     * <p>

     * <code>ClassName</code> is the name of the class to generate. The
     * prefixes in cdsPath are each used as the package name until a
     * matching class is found. Then an instance is created with the
     * parseCDS(String[]) method and it is passed the parameters fromthe
     * CDS.<p>

     * The <code>lookup(fingerprint)</code> CDS assumes
     * <code>fingerprint</code> to be a CDS for a Fingerprint object. It
     * then looks up the key with the specified fingerprint in
     * <code>keySource</code>.<p>

     * This method may throw exceptions with very long, nested explanations
     * if an exception occurs in a sub-CDS.

     * @see #keySource
     * @exception IOException if an error occured reading characers from <code>in</code>
     * @exception InvalidCDSException if the CDS is in some way malformed.
     */
    public static Object fromString(Reader cds) throws InvalidCDSException, IOException
    {
        // PARSE CDS

        StringBuffer sb=new StringBuffer();

        int ch=pastSpace(cds);
        if(ch==-1)
            return null;
        while(Character.isJavaIdentifierPart((char)ch) && (ch!=-1)) {
            sb.append((char)ch);
            ch=cds.read();
        }
        String name=sb.toString();

        sb=new StringBuffer();
        if(Character.isWhitespace((char)ch)) {
            ch=pastSpace(cds);
        }
        if(ch!='(')
            throw new InvalidCDSException("( expected after "+name);
        String[] params = splitParams(cds);

        // FIND OR CREATE OBJECT
        if(name.equals("lookup")) {
            // Look up an object by it's fingerprint
            if(params.length!=1) {
                throw new InvalidCDSException("Trying to lookup() more than one object at a time");
            }
            Object f= fromString(params[0]);
            if(!(f instanceof Fingerprint)) {
                throw new InvalidCDSException("Fingerprint object expected as parameter to lookup()");
            }
            Fingerprint fi=(Fingerprint)f;
            if(keySource==null) {
                throw new InvalidCDSException("No key-source has been assigned");
            }
            return keySource.byFingerprint(fi);
        } else {
            // Construct an object by name
            Class cl = makeClass(name);
            Method parseCDS;
            try {
                Class[] parType = { params.getClass() };
                parseCDS=cl.getMethod("parseCDS", parType);
            } catch (Exception e) {
                throw new InvalidCDSException(name+" does not have a "+name+"(String[]) method");
            }

            Object r;
            try {
                Object[] args = { params };
                r = parseCDS.invoke(null,args);
            } catch (InvocationTargetException e1) {
                throw new InvalidCDSException("Unable to create an instance of "+name+" [ "+e1.getTargetException().toString()+" ]");
            }
            catch (Exception e) {
                throw new InvalidCDSException("Unable to create an instance of "+name);
            }
            return r;
        }
    }


    /**
     * Parse the given Cipher Description String (CDS). This method calls the
     * <code>fromString(Reader)</code> method after wrapping th cds in a
     * StringReader.
     *
     * @exception InvalidCDSException if the CDS is in some way malformed.
     */
    public static Object fromString(String cds) throws InvalidCDSException
    {
        try {
            return fromString(new StringReader(cds));
        } catch (IOException e) {
            // StringReader doesn't actually throw any exceptions.
            return null;
        }
    }


    /**
     * Print this object to out, indented with ind tabs, going down at most
     * rec levels of recursion. */
    public void prettyPrint(PrintWriter out, int ind, int rec) throws IOException
    {
        if(rec<0)
            return;
        for(int i=0; i<ind; i++)
            out.print('\t');
        out.print(this.toString());
    }


    /**
     * Print this object to out, indented with ind tabs, going down at most
     * rec levels of recursion. */
    public void prettyPrint(PrintWriter out) throws IOException
    {
        prettyPrint(out, 0, 10000);
    }


}
