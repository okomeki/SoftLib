package net.siisise.security;

import java.security.MessageDigest;
import net.siisise.io.PacketA;

/**
 * RFC 1321 MD5の実装.
 * RFC 6151?
 * @author okome
 */
public class MD5 extends MessageDigest {

    public static String OBJECTIDENTIFIER = "1.2.840.113549.2.5";
    
    private int[] ad;

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
        ad = new int[] {0x67452301,0xefcdab89,0x98badcfe,0x10325476};
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
    
    static final int[] S1 = {7,22,17,12};
    static final int[] S2 = {5,20,14,9};
    static final int[] S3 = {4,23,16,11};
    static final int[] S4 = {6,21,15,10};
    static final int[] K = {
        0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee,
        0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
        0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
        0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,

        0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa,
        0xd62f105d, 0x2441453, 0xd8a1e681, 0xe7d3fbc8,
        0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed,
        0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,

        0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
        0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
        0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x4881d05,
        0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,

        0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039,
        0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
        0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
        0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391
    };

    private void abcdf(int m, int i) {
        int e = -i & 3;
        int b = ad[(1-i) & 3];
        int c = ad[(2-i) & 3];
        int d = ad[(3-i) & 3];
        m += ad[e] + ((b & c) | ~b & d) + K[i];
        d = S1[e];
        ad[e] = ((m << d) | (m >>> (32-d))) + b;
    }

    private void abcdg(int m, int i) {
        int e = -i & 3;
        int b = ad[(1-i) & 3];
        int c = ad[(2-i) & 3];
        int d = ad[(3-i) & 3];
        m += ad[e] + ((b & d) | (~d & c)) + K[i];
        d = S2[e];
        ad[e] = ((m << d) | (m >>> (32-d))) + b;
    }

    private void abcdh(int m, int i) {
        int e = -i & 3;
        int b = ad[(1-i) & 3];
        int c = ad[(2-i) & 3];
        int d = ad[(3-i) & 3];
        m += ad[e] + (b ^ c ^ d) + K[i];
        d = S3[e];
        ad[e] = ((m << d) | (m >>> (32-d))) + b;
    }

    private void abcdi(int m, int i) {
        int e = -i & 3;
        int b = ad[(1-i) & 3];
        int c = ad[(2-i) & 3];
        int d = ad[(3-i) & 3];
        m += ad[e] + (c ^ (b | ~d)) + K[i];
        d = S4[e];
        ad[e] = ((m << d) | (m >>> (32-d))) + b;
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
                aa = ad[0];
                bb = ad[1];
                cc = ad[2];
                dd = ad[3];

                /* Round 1. */
                for ( int i = 0; i < 16; i++ ) {
                    abcdf(x[i],i);
                }
                /* Round 2. */
                for ( int i = 16; i < 32; i++ ) {
                    abcdg(x[(i*5 + 1) & 0x0f],i);
                }
                /* Round 3. */
                for ( int i = 32; i < 48; i++ ) {
                    abcdh(x[(i*3 + 5) & 0x0f],i);
                }
                /* Round 4. */
                for ( int i = 48; i < 64; i++ ) {
                    abcdi(x[(i*7) & 0x0f],i);
                }

                ad[0] += aa;
                ad[1] += bb;
                ad[2] += cc;
                ad[3] += dd;
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
            lena[i] = (byte) len;
            len >>>= 8;
        }

        engineUpdate(lena,0,lena.length);
        
        byte[] ret = new byte[16];
        for ( int i = 0; i < 16; i++ ) {
            ret[i] = (byte) (ad[i/4] >>> ((i& 3) * 8));
        }
        engineReset();
        return ret;
    }
}
