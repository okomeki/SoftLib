package net.siisise.security;

import java.security.MessageDigest;
import net.siisise.io.PacketA;

/**
 * RFC 3174 SHA-1.
 * FIPS 180-1.
 */
public class SHA1 extends MessageDigest {

    public static String OBJECTIDENTIFIER = "1.3.14.3.2.26";
    private int[] h = new int[5];
    private PacketA pac;
    private long length;

    public SHA1() {
        super("SHA-1");
        engineReset();
    }

    @Override
    protected int engineGetDigestLength() {
        return 20;
    }

    @Override
    protected final void engineReset() {

        h[0] = 0x67452301;
        h[1] = 0xefcdab89;
        h[2] = 0x98badcfe;
        h[3] = 0x10325476;
        h[4] = 0xc3d2e1f0;
        pac = new PacketA();
        length = 0;
    }

    private static int fk(int t, int b, int c, int d) {
        if (t <= 19) { // Ch(x,y,z)
//            return ((b & c) | ((~b) & d)) + 0x5a827999;
            return (d ^ (b & (c ^ d))) + 0x5a827999;
        } else if (t <= 39) { // Parity(x,y,z)
            return (b ^ c ^ d) + 0x6ed9eba1;
        } else if (t <= 59) { // Maj(x,y,z)
//            return ((b & c) | (b & d) | (c & d)) + 0x8f1bbcdc;
            return ((b & c) | ((b | c) & d)) + 0x8f1bbcdc;
        } // Parity(x,y,z)
        return (b ^ c ^ d) + 0xca62c1d6;
    }

    @Override
    protected void engineUpdate(byte input) {
        engineUpdate(new byte[]{input}, 0, 1);
    }

    /**
     *
     * @param src
     */
    @Override
    protected void engineUpdate(byte[] src, int offset, int len) {
        pac.write(src, offset, len);
        length += len * 8l;

        int w[] = new int[80];
        while (pac.length() >= 64) {
            int a, b, c, d, e;
            // 6.1.
            // c.
            a = h[0];
            b = h[1];
            c = h[2];
            d = h[3];
            e = h[4];
            // a.
            for (int t = 0; t < 16; t++) {
                w[t] = (pac.read() << 24) + (pac.read() << 16) + (pac.read() << 8) + pac.read();
            }
            // b.
            for (int t = 16; t < 80; t++) {
                int n = w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16];
                w[t] = (n << 1) | (n >>> 31);
            }
            // d.
            for (int t = 0; t < 80; t++) {
                int temp = ((a << 5) | (a >>> 27)) + fk(t, b, c, d) + e + w[t];
                e = d;
                d = c;
                c = (b << 30) | (b >>> 2);
                b = a;
                a = temp;
            }
            // e.
            h[0] += a;
            h[1] += b;
            h[2] += c;
            h[3] += d;
            h[4] += e;
        }
    }

    /**
     *
     * @return はっしゅ
     */
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
            lena[7-i] = (byte) (len & 0xff);
            len >>>= 8;
        }

        engineUpdate(lena, 0, lena.length);

        byte[] ret = SHA256.toB(h);
        engineReset();
        return ret;
    }

}
