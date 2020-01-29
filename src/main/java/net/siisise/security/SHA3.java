package net.siisise.security;

import java.security.MessageDigest;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;

/**
 * SHA-3 Standard: Permutation-Based Hash and Extendable-Output Functions (FIPS
 * PUB 202).
 * Secure Hash Algorithm-3 (SHA-3) family. w=64 (long) で最適化したもの
 * SHA3-224, SHA3-256, SHA3-384, SHA3-512 に対応
 * 
 * @author okome
 */
public class SHA3 extends MessageDigest {

    static final int l = 6;
    protected static final int w = 1 << l; // 2^l bit l = 6

    // ハッシュ長
    protected int n;
    // 入出力分割ビット数?
    protected int r;
    protected int R;
    // キャパシティ
//    protected int c;

    protected Packet pac;
    // bit
    protected long length;

    long[] a;

    static final long[] RC = new long[24];
    
    static {
        for ( int ir = 0; ir < 24; ir++ ) {
            RC[ir] = 0;
            for (int j = 0; j <= l; j++) { // l = 6
                // little endian
                RC[ir] |= rc(j + 7 * ir) ? (1l << ((1<<j) -1)) : 0;
                // big endian
//                RC |= rc(j + 7 * ir) ? (1l << (64 - (1<<j))) : 0;
            }
        }
    }

    /**
     *
     * @param n 224,256,384,512
     */
    public SHA3(int n) {
        super("SHA3-" + n);
        this.n = n;
        engineReset();
    }

    @Override
    protected void engineReset() {
        int c = 2 * n; // キャパシティ 224が32の倍数なので倍
        //w = 64;
        // 200 - 56,64,96,128 * 8
        r = 5 * 5 * w - c; // 1600-c 448,512,768,1024 
        // R = 25 - 7,8,12,16 18,17,13,9
        R = r / 64;
        pac = new PacketA();
        length = 0;
        a = new long[5*5];
    }

    @Override
    protected void engineUpdate(byte input) {
        engineUpdate(new byte[]{input}, 0, 1);
    }

    // little endian
    private static final long ROTL(final long x, final long n) {
        return (x >>> (64 - n)) | (x << n);
    }

    // big endian
//    static final long ROTR(final long x, final long n) {
//        return (x >>> n) | (x << (64 - n));
//    }

    /**
     * 3.2.1.
     * Algorithm 1
     * @param a A
     * @return A'
     */
    private static final long[] Θ(long[] a) {

        long[] c = new long[5];
        long[] d = new long[5];
        // 3.2.1 Θ
        // Step 1.
        for (int x = 0; x < 5; x++) {
            c[x] = a[x] ^ a[x + 5] ^ a[x + 10] ^ a[x + 15] ^ a[x + 20];
        }
        // Step 2.
        for (int x = 0; x < 5; x++) {
            d[x] = c[(x + 4) % 5] ^ ROTL(c[(x + 1) % 5], 1);
        }
        // Step 3.
        for (int b = 0; b < 25; b++) {
            int x = b % 5;
            a[b] ^= d[x];
        }
        return a;
    }

    /**
     * Algorithm 5. 事前計算可能
     * bitなので big endian で計算している
     */
    static boolean rc(int t) {
        if (t % 255 == 0) {
            return true;
        }
        int R = 0x80;
        for (int i = 1; i <= t % 255; i++) {
//            R = 0 | R; // 9bit?
            R = (R & 0xe3) | ((R & 0x11c) ^ ((R & 1) * 0x11c));
            R >>>= 1;
        }
        return (R & 0x80) != 0;
    }

    static final long[] rnd(long[] a, int ir) {
        long[] ad = new long[25];
        a = Θ(a);
        // 3.2.2. ρ
        ad[0] = a[0];
        int x = 1;
        int y = 0;
        for (int t = 0; t < 24; t++) {
            ad[x + y * 5] = ROTL(a[x + y * 5], ((t + 1) * (t + 2) / 2) % w);
            int nx = y;
            y = (2 * x + 3 * y) % 5;
            x = nx;
        }

        // 3.2.3 π
        for (y = 0; y < 5; y++) {
            for (x = 0; x < 5; x++) {
                a[x + 5*y] = ad[((3*x + y) % 5) + 5*y];
            }
        }

        // 3.2.4 χ
        for (y = 0; y < 5; y++) {
            for (x = 0; x < 5; x++) {
                ad[x + y*5] = a[y + x*5] ^ ((~a[y + ((x+1) % 5)*5]) & a[y + ((x+2) % 5)*5]);
            }
        }
        
        ad[0] ^= RC[ir];
        return ad;
    }

    /**
     * 
     * @param s Aに変換済み
     * @param nr
     * @return A' S'には変換しない
     */
    final long[] keccak_f(long[] s) {

        for (int ir = 0; ir < 12 + 2*l; ir++) {
            s = rnd(s, ir);
        }
        return s;
    }

    /**
     * Algorithm 8
     * @param b
     * @return 
     */
    void sponge(byte[] b) {
        for (int c = 0; c < R; c++) {
            for (int j = 0; j < 8; j++) {
                a[c] ^= (((long) b[8 * c + j] & 0xff)) << (j * 8);
            }
        }
        a = keccak_f(a);
    }
    
    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        pac.write(input, offset, len);
        length += len * 8l;

        byte[] dd = new byte[R * 8];
        while (pac.length() >= R * 8) {
            pac.read(dd);
            sponge(dd);
        }
    }

    @Override
    protected byte[] engineDigest() {

        // padding バイト長で計算
        int rblen = R * 8;
        int padlen = rblen - (int) ((length / 8 + 1) % rblen) + 1;
        byte[] pad = new byte[padlen];
        pad[0] |= 0x06;
        pad[padlen - 1] |= 0x80;

        engineUpdate(pad, 0, pad.length);

        byte[] digest = new byte[n / 8];
        int i = 0;
        for (int c = 0; c < R; c++) {
            for (int j = 0; j < 8; j++) {
                digest[i++] = (byte) (a[c] >>> ((j) * 8));
                if ( i == n/8) {
                    break;
                }
            }
        }
        engineReset();
        return digest;
    }
}
