// Copyright (C) 1998-2000 Logi Ragnarsson

package org.logi.crypto.sign;

/**
 * An object of this class holds the state of an MD5 fingerprint still
 * being calculated.
 *
 * @see org.logi.crypto.sign.Fingerprint
 * @see org.logi.crypto.sign.Signature
 *
 * @author <a href="http://www.logi.org/~logir/">Logi Ragnarsson</a>
 *         (<a href="mailto:logir@logi.org">logir@logi.org</a>)
 */
public class MD5State extends HashState
{

    // STATIC UTILITY FUNCTIONS

    // U: x = toIntArray(buffer, len, offset)
    // B: len <= buffer.length, len <= 64.
    // A: x contains the bits from buffer[offset..offset+len-1].
    private static int[] toIntArray (byte buffer[], int offset, int len)
    {
        int[] out = new int[16];
        int i, j;

        for (i = j = 0; j < len; i++, j += 4) {
            out[i] = ((int) (buffer[j + offset] & 0xff))        |
                     (((int) (buffer[j + 1 + offset] & 0xff)) << 8)  |
                     (((int) (buffer[j + 2 + offset] & 0xff)) << 16) |
                     (((int) (buffer[j + 3 + offset] & 0xff)) << 24);
        }

        return out;
    }

    // U: b = toByteArray(buffer, len)
    // B: buffer.length >= 4*len
    // A: b vontains the bits from buffer[0..len-1]
    private static byte[] toByteArray (int[] buffer, int len)
    {
        int    i, j;
        byte[] out = new byte[len];

        for (i = j = 0; j  < len; i++, j += 4) {
            out[j] = (byte) (buffer[i] & 0xff);
            out[j + 1] = (byte) ((buffer[i] >>> 8) & 0xff);
            out[j + 2] = (byte) ((buffer[i] >>> 16) & 0xff);
            out[j + 3] = (byte) ((buffer[i] >>> 24) & 0xff);
        }

        return out;
    }


    // TRANSIENT MD5 STATE

    /**
     * MD5 function without the padding.
     */
    public static class SubState
    {

        /**
         * A hash for as much of the data added to this object as
         * possible, given that they must be added in 64 byte chunks.
         */
        public int[] hash;

        /** Holds the number of bits added to this object. */
        public long count;

        /**
         * Holds the bytes that have been added to this object, but
         * not calculated into the hash in <code>state</code>.
         */
        public byte buffer[];

        /** Create a new empty instance */
        public SubState()
        {
            buffer = new byte[64];
            hash  = new int[4];
            reset();
        }

        /** Reset the internal state */
        public void reset()
        {
            hash[0] = 0x67452301;
            hash[1] = 0xefcdab89;
            hash[2] = 0x98badcfe;
            hash[3] = 0x10325476;
            count=0;
        }

        /** Create a copy of s */
        public SubState (SubState s)
        {
            buffer = new byte[64];
            hash = new int[4];

            System.arraycopy(s.hash, 0, hash, 0, 4);
            System.arraycopy(s.buffer, 0, buffer, 0, 64);

            count = s.count;
        }

        public static int rotateLeft (int x, int n)
        {
            return (x << n) | (x >>> (32 - n));
        }

        public static int uadd (int a, int b)
        {
            long aa = ((long) a) & 0xffffffffL;
            long bb = ((long) b) & 0xffffffffL;
            return (int) ((aa+bb) & 0xffffffffL);
        }

        public static int uadd (int a, int b, int c)
        {
            long aa = ((long) a) & 0xffffffffL;
            long bb = ((long) b) & 0xffffffffL;
            long cc = ((long) c) & 0xffffffffL;
            return (int) ((aa+bb+cc) & 0xffffffffL);
        }

        public static int uadd (int a, int b, int c, int d)
        {
            long aa = ((long) a) & 0xffffffffL;
            long bb = ((long) b) & 0xffffffffL;
            long cc = ((long) c) & 0xffffffffL;
            long dd = ((long) d) & 0xffffffffL;
            return (int) ((aa+bb+cc+dd) & 0xffffffffL);
        }

        public static int FF (int a, int b, int c, int d, int x, int s, int ac)
        {
            a = uadd(a, ((b & c) | (~b & d)), x, ac);
            return uadd(rotateLeft(a, s), b);
        }

        public static int GG (int a, int b, int c, int d, int x, int s, int ac)
        {
            a = uadd(a, ((b & d) | (c & ~d)), x, ac);
            return uadd(rotateLeft(a, s), b);
        }

        public static int HH (int a, int b, int c, int d, int x, int s, int ac)
        {
            a = uadd(a, (b ^ c ^ d), x, ac);
            return uadd(rotateLeft(a, s) , b);
        }

        public static int II (int a, int b, int c, int d, int x, int s, int ac)
        {
            a = uadd(a, (c ^ (b | ~d)), x, ac);
            return uadd(rotateLeft(a, s), b);
        }

        // U: transform(data, offset);
        // B: buffer[shift..shift+63] exists.
        // A: the bytes in buffer[offset, offset+63] have been added to state.
        public void transform (byte data[], int offset)
        {
            int a = hash[0];
            int b = hash[1];
            int c = hash[2];
            int d = hash[3];
            int[] x = toIntArray(data, offset, 64);

            // Round 1
            a = FF (a, b, c, d, x[ 0],   7, 0xd76aa478); // 1
            d = FF (d, a, b, c, x[ 1],  12, 0xe8c7b756); // 2
            c = FF (c, d, a, b, x[ 2],  17, 0x242070db); // 3
            b = FF (b, c, d, a, x[ 3],  22, 0xc1bdceee); // 4
            a = FF (a, b, c, d, x[ 4],   7, 0xf57c0faf); // 5
            d = FF (d, a, b, c, x[ 5],  12, 0x4787c62a); // 6
            c = FF (c, d, a, b, x[ 6],  17, 0xa8304613); // 7
            b = FF (b, c, d, a, x[ 7],  22, 0xfd469501); // 8
            a = FF (a, b, c, d, x[ 8],   7, 0x698098d8); // 9
            d = FF (d, a, b, c, x[ 9],  12, 0x8b44f7af); // 10
            c = FF (c, d, a, b, x[10],  17, 0xffff5bb1); // 11
            b = FF (b, c, d, a, x[11],  22, 0x895cd7be); // 12
            a = FF (a, b, c, d, x[12],   7, 0x6b901122); // 13
            d = FF (d, a, b, c, x[13],  12, 0xfd987193); // 14
            c = FF (c, d, a, b, x[14],  17, 0xa679438e); // 15
            b = FF (b, c, d, a, x[15],  22, 0x49b40821); // 16

            // Round 2
            a = GG (a, b, c, d, x[ 1],   5, 0xf61e2562); // 17
            d = GG (d, a, b, c, x[ 6],   9, 0xc040b340); // 18
            c = GG (c, d, a, b, x[11],  14, 0x265e5a51); // 19
            b = GG (b, c, d, a, x[ 0],  20, 0xe9b6c7aa); // 20
            a = GG (a, b, c, d, x[ 5],   5, 0xd62f105d); // 21
            d = GG (d, a, b, c, x[10],   9,  0x2441453); // 22
            c = GG (c, d, a, b, x[15],  14, 0xd8a1e681); // 23
            b = GG (b, c, d, a, x[ 4],  20, 0xe7d3fbc8); // 24
            a = GG (a, b, c, d, x[ 9],   5, 0x21e1cde6); // 25
            d = GG (d, a, b, c, x[14],   9, 0xc33707d6); // 26
            c = GG (c, d, a, b, x[ 3],  14, 0xf4d50d87); // 27
            b = GG (b, c, d, a, x[ 8],  20, 0x455a14ed); // 28
            a = GG (a, b, c, d, x[13],   5, 0xa9e3e905); // 29
            d = GG (d, a, b, c, x[ 2],   9, 0xfcefa3f8); // 30
            c = GG (c, d, a, b, x[ 7],  14, 0x676f02d9); // 31
            b = GG (b, c, d, a, x[12],  20, 0x8d2a4c8a); // 32

            // Round 3
            a = HH (a, b, c, d, x[ 5],   4, 0xfffa3942); // 33
            d = HH (d, a, b, c, x[ 8],  11, 0x8771f681); // 34
            c = HH (c, d, a, b, x[11],  16, 0x6d9d6122); // 35
            b = HH (b, c, d, a, x[14],  23, 0xfde5380c); // 36
            a = HH (a, b, c, d, x[ 1],   4, 0xa4beea44); // 37
            d = HH (d, a, b, c, x[ 4],  11, 0x4bdecfa9); // 38
            c = HH (c, d, a, b, x[ 7],  16, 0xf6bb4b60); // 39
            b = HH (b, c, d, a, x[10],  23, 0xbebfbc70); // 40
            a = HH (a, b, c, d, x[13],   4, 0x289b7ec6); // 41
            d = HH (d, a, b, c, x[ 0],  11, 0xeaa127fa); // 42
            c = HH (c, d, a, b, x[ 3],  16, 0xd4ef3085); // 43
            b = HH (b, c, d, a, x[ 6],  23,  0x4881d05); // 44
            a = HH (a, b, c, d, x[ 9],   4, 0xd9d4d039); // 45
            d = HH (d, a, b, c, x[12],  11, 0xe6db99e5); // 46
            c = HH (c, d, a, b, x[15],  16, 0x1fa27cf8); // 47
            b = HH (b, c, d, a, x[ 2],  23, 0xc4ac5665); // 48

            // Round 4
            a = II (a, b, c, d, x[ 0],   6, 0xf4292244); // 49
            d = II (d, a, b, c, x[ 7],  10, 0x432aff97); // 50
            c = II (c, d, a, b, x[14],  15, 0xab9423a7); // 51
            b = II (b, c, d, a, x[ 5],  21, 0xfc93a039); // 52
            a = II (a, b, c, d, x[12],   6, 0x655b59c3); // 53
            d = II (d, a, b, c, x[ 3],  10, 0x8f0ccc92); // 54
            c = II (c, d, a, b, x[10],  15, 0xffeff47d); // 55
            b = II (b, c, d, a, x[ 1],  21, 0x85845dd1); // 56
            a = II (a, b, c, d, x[ 8],   6, 0x6fa87e4f); // 57
            d = II (d, a, b, c, x[15],  10, 0xfe2ce6e0); // 58
            c = II (c, d, a, b, x[ 6],  15, 0xa3014314); // 59
            b = II (b, c, d, a, x[13],  21, 0x4e0811a1); // 60
            a = II (a, b, c, d, x[ 4],   6, 0xf7537e82); // 61
            d = II (d, a, b, c, x[11],  10, 0xbd3af235); // 62
            c = II (c, d, a, b, x[ 2],  15, 0x2ad7d2bb); // 63
            b = II (b, c, d, a, x[ 9],  21, 0xeb86d391); // 64

            hash[0] += a;
            hash[1] += b;
            hash[2] += c;
            hash[3] += d;
        }


    }

    /** Holds the actual state of the object. */
    private SubState state;

    /**
     * Either <code>null</code> or a valid Fingerprint object for the
     * current state.
     */
    private Fingerprint valid;

    /** Padding to add to data to get a mutiple of 64 bytes. */
    static byte[] padding = makePadding();

    static byte[] makePadding()
    {
        byte[] r = new byte[64];
        r[0]=(byte)0x80;
        return r;
    }

    /** Create a new clear MD5State. */
    public MD5State()
    {
        state = new SubState();
        valid = null;
    }

    /** The name of the algorithm is "MD5". */
    public String getName()
    {
        return "MD5";
    }

    /** Reset the object. */
    public void reset()
    {
        state.reset();
        valid = null;
    }

    // Update the state stat with the bytes from
    // data[offset..offset+length-1]
    private synchronized void update(SubState stat, byte[] data, int offset, int length)
    {
        valid = null;

        if ((length-offset)> data.length)
            length = data.length - offset;
        // buf[offset..offset+length-1] will be added to stat.

        int index = (int)(stat.count/8) % 64;
        // index is the number of bytes in stat, modulo 64

        stat.count += 8*length;
        // stat.count has been updated as if the data had been added.

        int partlen = 64 - index;
        // partlen is the number of bytes to add to stat, so that it
        // contains an even multiple of 64 bytes.

        int i;
        if (length >= partlen) {
            System.arraycopy(data,offset, stat.buffer,index, partlen);

            stat.transform(stat.buffer, 0);
            // partlen bytes have been added to stat, which now contains
            // an even multiple of 64 bytes.

            for (i=partlen; (i + 63) < length; i+= 64)
                stat.transform(data, i);

            // i is the number of bytes that have been added to stat, which
            // still contains an even multiple of 64 bytes.

            // The number bytes that have not been added to
            // stat is less than 64.

            index = 0;
        } else
            i = 0;

        if (i < length) {
            int start = i;
            for (; i < length; i++)
                stat.buffer[index + i - start] = data[i + offset];
        }
        // Remaining data has been buffered in stat.

        // All data from buf has been added to stat, either directly or
        // buffered if an even multiple of 64 bytes was not achieved.
    }

    /**
     * Update the fingerprint state with the bytes from
     * <code>buf[offset, offset+length-1]</code>. */
    public void update(byte[] data, int offset, int length)
    {
        update(state, data, offset, length);
    }

    /**
     * Return a Fingerprint for the curret state, without
     * destroying the state. */
    public Fingerprint calculate()
    {
        if (valid == null) {
            SubState fin = new SubState(state);

            byte[] count = {
                               (byte)(fin.count),
                               (byte)(fin.count >>  8),
                               (byte)(fin.count >> 16),
                               (byte)(fin.count >> 24),
                               (byte)(fin.count >> 32),
                               (byte)(fin.count >> 40),
                               (byte)(fin.count >> 58),
                               (byte)(fin.count >> 56)
                           };

            int index = (int)(fin.count/8) % 64;
            int padlen = (index < 56) ? (56 - index) : (120 - index);

            update(fin, padding, 0, padlen);
            update(fin, count, 0, 8);

            byte[] buf=toByteArray(fin.hash,16);
            valid = new Fingerprint("MD5", buf,0,buf.length);
        }
        return valid;
    }

    /**
     * Return the size of input-blocks for this hash function in bytes. */
    public int blockSize()
    {
        return 8;
    }

    /**
     * Returns the size of a fingerprint in bytes. */
    public int hashSize()
    {
        return 16;
    }

}
