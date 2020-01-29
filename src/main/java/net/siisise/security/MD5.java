package net.siisise.security;

import java.security.MessageDigest;
import net.siisise.io.PacketA;

/**
 * RFC 1321 MD5の実装.
 *
 * @author okome
 */
public class MD5 extends MessageDigest {

    public static String OBJECTIDENTIFIER = "1.2.840.113549.2.5";
    
    private int a;
    private int b;
    private int c;
    private int d;

    private PacketA pac;
    private int length = 0;

    public MD5() {
        super("MD5");
        engineReset();
    }
    
    @Override
    protected int engineGetDigestLength() {
        return 16;
    }

    @Override
    protected void engineReset() {
        a = 0x67452301;
        b = 0xefcdab89;
        c = 0x98badcfe;
        d = 0x10325476;
        length = 0;
        pac = new PacketA();
    }

    private static int m(PacketA pac) {
        int r;
        r = pac.read() & 0xff;
        r |= (pac.read() & 0xff) << 8;
        r |= (pac.read() & 0xff) << 16;
        r |= (pac.read() & 0xff) << 24;
        return r;
    }
    
    private static int abcdf(int b, int c, int d, int k, int s, int t) {
        int a = ((b & c) | ((~b) & d)) + k + t;
        return (a << s) + (a >>> (32 - s)) + b;
    }

    private static int abcdg(int a, int b, int c, int d, int k, int s, int t) {
        a += ((b & d) | (c & (~d))) + k + t;
        return (a << s) + (a >>> (32 - s)) + b;
    }

    private static int abcdh(int a, int b, int c, int d, int k, int s, int t) {
        a += (b ^ c ^ d) + k + t;
        return (a << s) + (a >>> (32 - s)) + b;
    }

    private static int abcdi(int a, int b, int c, int d, int k, int s, int t) {
        a = a + (c ^ (b | (~d))) + k + t;
        return (a << s) + (a >>> (32 - s)) + b;
    }
    
    @Override
    protected void engineUpdate(byte input) {
        engineUpdate(new byte[] {input},0,1);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
//        pac.write(src);
        length += len * 8l;
        
        if ( len > 64 ) { //残る部分を分けるだけ
            pac.write(input,offset,len - 64);
            offset += len - 64;
            len = 64;
        }
        pac.write(input,offset,len);

        if (pac.length() >= 64) {
            
            int aa, bb, cc, dd;
            int x[] = new int[16];
            while (pac.length() >= 64) {
                for (int j = 0; j < 16; j++) {
                    x[j] = m(pac);
                }
                aa = a;
                bb = b;
                cc = c;
                dd = d;

                /* Round 1. */
                a = abcdf(b, c, d, a + x[0], 7, 0xd76aa478);
                d = abcdf(a, b, c, d + x[1], 12, 0xe8c7b756);
                c = abcdf(d, a, b, c + x[2], 17, 0x242070db);
                b = abcdf(c, d, a, b + x[3], 22, 0xc1bdceee);

                a = abcdf(b, c, d, a + x[4], 7, 0xf57c0faf);
                d = abcdf(a, b, c, d + x[5], 12, 0x4787c62a);
                c = abcdf(d, a, b, c + x[6], 17, 0xa8304613);
                b = abcdf(c, d, a, b + x[7], 22, 0xfd469501);

                a = abcdf(b, c, d, a + x[8], 7, 0x698098d8);
                d = abcdf(a, b, c, d + x[9], 12, 0x8b44f7af);
                c = abcdf(d, a, b, c + x[10], 17, 0xffff5bb1);
                b = abcdf(c, d, a, b + x[11], 22, 0x895cd7be);

                a = abcdf(b, c, d, a + x[12], 7, 0x6b901122);
                d = abcdf(a, b, c, d + x[13], 12, 0xfd987193);
                c = abcdf(d, a, b, c + x[14], 17, 0xa679438e);
                b = abcdf(c, d, a, b + x[15], 22, 0x49b40821);

                /* Round 2. */
                a = abcdg(a, b, c, d, x[1], 5, 0xf61e2562);
                d = abcdg(d, a, b, c, x[6], 9, 0xc040b340);
                c = abcdg(c, d, a, b, x[11], 14, 0x265e5a51);
                b = abcdg(b, c, d, a, x[0], 20, 0xe9b6c7aa);

                a = abcdg(a, b, c, d, x[5], 5, 0xd62f105d);
                d = abcdg(d, a, b, c, x[10], 9, 0x2441453);
                c = abcdg(c, d, a, b, x[15], 14, 0xd8a1e681);
                b = abcdg(b, c, d, a, x[4], 20, 0xe7d3fbc8);

                a = abcdg(a, b, c, d, x[9], 5, 0x21e1cde6);
                d = abcdg(d, a, b, c, x[14], 9, 0xc33707d6);
                c = abcdg(c, d, a, b, x[3], 14, 0xf4d50d87);
                b = abcdg(b, c, d, a, x[8], 20, 0x455a14ed);

                a = abcdg(a, b, c, d, x[13], 5, 0xa9e3e905);
                d = abcdg(d, a, b, c, x[2], 9, 0xfcefa3f8);
                c = abcdg(c, d, a, b, x[7], 14, 0x676f02d9);
                b = abcdg(b, c, d, a, x[12], 20, 0x8d2a4c8a);

                /* Round 3. */
                a = abcdh(a, b, c, d, x[5], 4, 0xfffa3942);
                d = abcdh(d, a, b, c, x[8], 11, 0x8771f681);
                c = abcdh(c, d, a, b, x[11], 16, 0x6d9d6122);
                b = abcdh(b, c, d, a, x[14], 23, 0xfde5380c);

                a = abcdh(a, b, c, d, x[1], 4, 0xa4beea44);
                d = abcdh(d, a, b, c, x[4], 11, 0x4bdecfa9);
                c = abcdh(c, d, a, b, x[7], 16, 0xf6bb4b60);
                b = abcdh(b, c, d, a, x[10], 23, 0xbebfbc70);

                a = abcdh(a, b, c, d, x[13], 4, 0x289b7ec6);
                d = abcdh(d, a, b, c, x[0], 11, 0xeaa127fa);
                c = abcdh(c, d, a, b, x[3], 16, 0xd4ef3085);
                b = abcdh(b, c, d, a, x[6], 23, 0x4881d05);

                a = abcdh(a, b, c, d, x[9], 4, 0xd9d4d039);
                d = abcdh(d, a, b, c, x[12], 11, 0xe6db99e5);
                c = abcdh(c, d, a, b, x[15], 16, 0x1fa27cf8);
                b = abcdh(b, c, d, a, x[2], 23, 0xc4ac5665);

                /* Round 4. */
                a = abcdi(a, b, c, d, x[0], 6, 0xf4292244);
                d = abcdi(d, a, b, c, x[7], 10, 0x432aff97);
                c = abcdi(c, d, a, b, x[14], 15, 0xab9423a7);
                b = abcdi(b, c, d, a, x[5], 21, 0xfc93a039);

                a = abcdi(a, b, c, d, x[12], 6, 0x655b59c3);
                d = abcdi(d, a, b, c, x[3], 10, 0x8f0ccc92);
                c = abcdi(c, d, a, b, x[10], 15, 0xffeff47d);
                b = abcdi(b, c, d, a, x[1], 21, 0x85845dd1);

                a = abcdi(a, b, c, d, x[8], 6, 0x6fa87e4f);
                d = abcdi(d, a, b, c, x[15], 10, 0xfe2ce6e0);
                c = abcdi(c, d, a, b, x[6], 15, 0xa3014314);
                b = abcdi(b, c, d, a, x[13], 21, 0x4e0811a1);

                a = abcdi(a, b, c, d, x[4], 6, 0xf7537e82);
                d = abcdi(d, a, b, c, x[11], 10, 0xbd3af235);
                c = abcdi(c, d, a, b, x[2], 15, 0x2ad7d2bb);
                b = abcdi(b, c, d, a, x[9], 21, 0xeb86d391);

                a += aa;
                b += bb;
                c += cc;
                d += dd;
            }
        }
    }
    
    @Override
    protected byte[] engineDigest() {

        long len = length;

        // ラスト周
        // padding
        pac.write(new byte[]{(byte) 0x80});
        int padlen = 512 - (int) ((len + 64 + 8) % 512);
        pac.write(new byte[padlen / 8]);
        byte[] lena = new byte[8];
        for ( int i = 0; i < 8; i++ ) {
            lena[i] = (byte) (len & 0xff);
            len >>>= 8;
        }

        engineUpdate(lena,0,lena.length);
        
        byte[] ret = new byte[16];
        ret[0] = (byte) (a & 0xff);
        ret[1] = (byte) ((a >>> 8) & 0xff);
        ret[2] = (byte) ((a >>> 16) & 0xff);
        ret[3] = (byte) ((a >>> 24) & 0xff);
        ret[4] = (byte) (b & 0xff);
        ret[5] = (byte) ((b >>> 8) & 0xff);
        ret[6] = (byte) ((b >>> 16) & 0xff);
        ret[7] = (byte) ((b >>> 24) & 0xff);
        ret[8] = (byte) (c & 0xff);
        ret[9] = (byte) ((c >>> 8) & 0xff);
        ret[10] = (byte) ((c >>> 16) & 0xff);
        ret[11] = (byte) ((c >>> 24) & 0xff);
        ret[12] = (byte) (d & 0xff);
        ret[13] = (byte) ((d >>> 8) & 0xff);
        ret[14] = (byte) ((d >>> 16) & 0xff);
        ret[15] = (byte) ((d >>> 24) & 0xff);
        engineReset();
        return ret;
    }
}
