package net.siisise.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import net.siisise.io.PacketA;

/**
 * RFC 6234
 */
public class SHA512 extends MessageDigest {

    public static int[] OBJECTIDENTIFIER = {2, 16, 840, 1, 101, 3, 4, 2, 3};
    protected long[] H;
    protected PacketA pac;
    protected BigInteger length;
    static long[] K = {
        0x428a2f98d728ae22l, 0x7137449123ef65cdl, 0xb5c0fbcfec4d3b2fl, 0xe9b5dba58189dbbcl,
        0x3956c25bf348b538l, 0x59f111f1b605d019l, 0x923f82a4af194f9bl, 0xab1c5ed5da6d8118l,
        0xd807aa98a3030242l, 0x12835b0145706fbel, 0x243185be4ee4b28cl, 0x550c7dc3d5ffb4e2l,
        0x72be5d74f27b896fl, 0x80deb1fe3b1696b1l, 0x9bdc06a725c71235l, 0xc19bf174cf692694l,
        0xe49b69c19ef14ad2l, 0xefbe4786384f25e3l, 0x0fc19dc68b8cd5b5l, 0x240ca1cc77ac9c65l,
        0x2de92c6f592b0275l, 0x4a7484aa6ea6e483l, 0x5cb0a9dcbd41fbd4l, 0x76f988da831153b5l,
        0x983e5152ee66dfabl, 0xa831c66d2db43210l, 0xb00327c898fb213fl, 0xbf597fc7beef0ee4l,
        0xc6e00bf33da88fc2l, 0xd5a79147930aa725l, 0x06ca6351e003826fl, 0x142929670a0e6e70l,
        0x27b70a8546d22ffcl, 0x2e1b21385c26c926l, 0x4d2c6dfc5ac42aedl, 0x53380d139d95b3dfl,
        0x650a73548baf63del, 0x766a0abb3c77b2a8l, 0x81c2c92e47edaee6l, 0x92722c851482353bl,
        0xa2bfe8a14cf10364l, 0xa81a664bbc423001l, 0xc24b8b70d0f89791l, 0xc76c51a30654be30l,
        0xd192e819d6ef5218l, 0xd69906245565a910l, 0xf40e35855771202al, 0x106aa07032bbd1b8l,
        0x19a4c116b8d2d0c8l, 0x1e376c085141ab53l, 0x2748774cdf8eeb99l, 0x34b0bcb5e19b48a8l,
        0x391c0cb3c5c95a63l, 0x4ed8aa4ae3418acbl, 0x5b9cca4f7763e373l, 0x682e6ff3d6b2b8a3l,
        0x748f82ee5defb2fcl, 0x78a5636f43172f60l, 0x84c87814a1f0ab72l, 0x8cc702081a6439ecl,
        0x90befffa23631e28l, 0xa4506cebde82bde9l, 0xbef9a3f7b2c67915l, 0xc67178f2e372532bl,
        0xca273eceea26619cl, 0xd186b8c721c0c207l, 0xeada7dd6cde0eb1el, 0xf57d4f7fee6ed178l,
        0x06f067aa72176fbal, 0x0a637dc5a2c898a6l, 0x113f9804bef90dael, 0x1b710b35131c471bl,
        0x28db77f523047d84l, 0x32caab7b40c72493l, 0x3c9ebe0a15c9bebcl, 0x431d67c49c100d4cl,
        0x4cc5d4becb3e42b6l, 0x597f299cfc657e2al, 0x5fcb6fab3ad6faecl, 0x6c44198c4a475817l
    };

    @Override
    protected void engineReset() {
        H = new long[]{
            0x6a09e667f3bcc908l,
            0xbb67ae8584caa73bl,
            0x3c6ef372fe94f82bl,
            0xa54ff53a5f1d36f1l,
            0x510e527fade682d1l,
            0x9b05688c2b3e6c1fl,
            0x1f83d9abfb41bd6bl,
            0x5be0cd19137e2179l
        };
        pac = new PacketA();
        length = BigInteger.valueOf(0);
    }

    public SHA512() {
        super("SHA-512");
        engineReset();
    }

    protected SHA512(String n) {
        super(n);
        engineReset();
    }

    static final long ch(final long x, final long y, final long z) {
        return (x & y) ^ ((~x) & z);
    }

    static final long maj(final long x, final long y, final long z) {
        return (x & y) ^ (x & z) ^ (y & z);
    }

    static final long ROTR(final long x, final long n) {
        return (x >>> n) | (x << (64 - n));
    }

    static final long Σ0(final long x) {
        return ROTR(x, 28) ^ ROTR(x, 34) ^ ROTR(x, 39);
    }

    static final long Σ1(final long x) {
        return ROTR(x, 14) ^ ROTR(x, 18) ^ ROTR(x, 41);
    }

    static final long σ0(final long x) {
        return ROTR(x, 1) ^ ROTR(x, 8) ^ (x >>> 7);
    }

    static final long σ1(final long x) {
        return ROTR(x, 19) ^ ROTR(x, 61) ^ (x >>> 6);
    }

    @Override
    protected void engineUpdate(byte input) {
        engineUpdate(new byte[]{input}, 0, 1);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        pac.write(input, offset, len);
        length = length.add(BigInteger.valueOf(len * 8l));

        long w[] = new long[80];
        while (pac.length() >= 128) {
            long a, b, c, d, e, f, g, h;

            a = H[0];
            b = H[1];
            c = H[2];
            d = H[3];
            e = H[4];
            f = H[5];
            g = H[6];
            h = H[7];

            for (int t = 0; t < 16; t++) {
                w[t] = (((long) pac.read()) << 56) + (((long) pac.read()) << 48)
                        + (((long) pac.read()) << 40) + (((long) pac.read()) << 32)
                        + (((long) pac.read()) << 24) + (pac.read() << 16)
                        + (pac.read() << 8) + pac.read();
                long temp1 = h + Σ1(e) + ch(e, f, g) + K[t] + w[t];
                long temp2 = Σ0(a) + maj(a, b, c);
                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;
            }

            for (int t = 16; t < 80; t++) {
                w[t] = σ1(w[t - 2]) + w[t - 7] + σ0(w[t - 15]) + w[t - 16];
                long temp1 = h + Σ1(e) + ch(e, f, g) + K[t] + w[t];
                long temp2 = Σ0(a) + maj(a, b, c);
                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;
            }
            H[0] += a;
            H[1] += b;
            H[2] += c;
            H[3] += d;
            H[4] += e;
            H[5] += f;
            H[6] += g;
            H[7] += h;
        }
    }

    static byte[] toB(long[] src) {
        byte[] ret = new byte[src.length * 8];
        for (int i = 0; i < src.length; i++) {
            long a = src[i];
            for ( int j = 7; j >= 0; j-- ) {
                ret[i*8+j] = (byte)(a & 0xff);
                a >>>= 8;
            }
        }
        return ret;
    }

    @Override
    protected byte[] engineDigest() {

        BigInteger len = length;
        byte[] lb = len.toByteArray();

        // ラスト周
        // padding
        pac.write(new byte[]{(byte) 0x80});
        int padlen = 1024 - (int) ((len.longValue() + lb.length * 8 + 8) % 1024);
        pac.write(new byte[padlen / 8]);

        engineUpdate(lb, 0, lb.length);

        byte[] ret = toB(H);
        engineReset();
        return ret;
    }
}
