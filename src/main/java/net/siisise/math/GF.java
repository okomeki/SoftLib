package net.siisise.math;

import java.math.BigInteger;
import java.util.Arrays;
import net.siisise.lang.Bin;

/**
 * ガロア体のなにか.
 * 8bit用とbyte列,long列用
 */
public class GF {

    // 8bit - 32bit?
    final int N;   // 7
    final int root; // 0x11b
    final byte constRb; // root と同じ 
    int size; // 255

    final int[] log;
    final int[] exp;

    // ガロア 有限体 原始多項式?
//  final byte FF4 = 0x3; // 0x13 10011
    public static final byte FF8 = 0x1b; // 0x11b 100011011 AES
    public static final byte FF64 = 0x1b; // 0x1000000000000001b x11011
    public static final byte FF128 = (byte) 0x87; // 0x100000000000000000000000000000087 x10000111 // GMAC
    public static final byte[] GF8 = {FF8}; // 0x11b
    public static final byte[] GF64 = {0, 0, 0, 0, 0, 0, 0, FF64}; // 0x1000000000000001b
    public static final byte[] GF128 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, FF128}; // 0x100000000000000000000000000000087 CMAC

    public GF() {
        this(8, 0x11b);
    }

    /**
     * 短い用 1バイトくらいの
     *
     * @param n 2^n 8 bit を想定
     * @param m n = 8のとき 0x11bくらい 上のビットあり
     */
    public GF(int n, int m) {
        N = n - 1;
        root = m;
        constRb = (byte) root; // root側を使う
        size = (1 << n) - 1;
        log = new int[size + 1];
        exp = new int[size + 1];

        int a = 1;
        for (int e = 0; e < size; e++) {
            log[a] = e;
            exp[e] = a;

            a ^= x(a); // a・3 // ・5 ・17でもいい
        }
        log[0] = 0;
        exp[size] = exp[0];
    }

    /**
     * rb は1バイトだけ使う仮実装
     *
     * @param n ビット数 128を想定
     * @param rb 0x87 を想定
     */
    public GF(int n, byte[] rb) {
        this(n, rb[rb.length - 1]);
    }

    /**
     * 長い用
     *
     * @param n ビット長 128bit
     * @param rb MSBを外したもの constやfinalなので複製しなくてもいい?
     */
    public GF(int n, byte rb) {
        N = n - 1;
        root = rb & 0xff; // constRb 側をつかう
        constRb = rb;
        log = null;
        exp = null;
    }

    /**
     * ふつうのGF s・2.
     * バイト数は未検証. てきとう.
     *
     * @param s 数
     * @return s・2
     */
    public byte[] x(byte[] s) {
        byte[] v = Bin.shl(s); // constRb に 1bit 持っているのでこっちは消す
        if (s[0] < 0) {
            v[v.length - 1] ^= constRb; // 長い用
        }
        return v;
    }

    /**
     * long列 GF s・2
     *
     * @param s 数
     * @return s・2
     */
    public long[] x(long[] s) {
        long[] v = Bin.shl(s); // constRb に 1bit 持っているのでこっちは消す
        v[v.length - 1] ^= (constRb & 0xffl) * (s[0] >>> 63);
        return v;
    }

    /**
     * GF s・2の逆 /2
     *
     * @param s・2
     * @return s
     */
    public byte[] r(byte[] s) {
        byte[] r = Bin.ror(s); // constRb の 1bit が消えるのでこっちでつける
        if (r[0] < 0) {
            r[r.length - 1] ^= (constRb & 0xff) >>> 1;
        }
        return r;
    }

    /**
     * GF s・2の逆 /2
     *
     * @param s・2
     * @return s
     */
    public int[] r(int[] s) {
        int[] r = Bin.ror(s); // constRb の 1bit が消えるのでこっちでつける
        if (r[0] < 0) {
            r[r.length - 1] ^= (constRb & 0xff) >>> 1;
        }
        return r;
    }

    /**
     * s / 2.
     * s・2ができるならs/2も比較的単純な計算でできる 2以外はできない
     *
     * @param s
     * @return
     */
    public long[] r(long[] s) {
        long[] r = Bin.ror(s); // constRb の 1bit が消えるのでこっちでつける
        if (r[0] < 0) {
            r[r.length - 1] ^= (constRb & 0xffl) >>> 1;
        }
        return r;
    }

    /**
     * a・2
     *
     * @param a
     * @return a・2
     */
    public final int x(int a) {
        return (a << 1) ^ ((a >>> N) * root);
    }

    /**
     *
     * @param s
     * @return
     */
    public int r(int s) {
        return (s >>> 1) ^ ((s & 1) * (root >>> 1));
    }

    /**
     * 8bit 逆数計算的なもの(高速版)
     *
     * @param a
     * @return aの逆数
     */
    public int inv(int a) {
        return a == 0 ? 0 : exp[size - log[a]];
    }

    static final BigInteger TWO = BigInteger.valueOf(2);
    static final BigInteger THREE = BigInteger.valueOf(3);
    static final BigInteger FIVE = BigInteger.valueOf(5);

    /**
     * 逆数計算的なもの(簡易版).
     * 256 - 2 で ^254 ぐらいの位置づけ
     * ビット長*2回掛けるぐらいで計算はできる
     *
     * @param a
     * @return aの逆数 60bit程度まで
     */
    public byte[] inv1(byte[] a) {
        return pow(a, TWO.shiftLeft(N).subtract(TWO));
    }

    public long[] inv1(long[] a) {
        return pow(a, TWO.shiftLeft(N).subtract(TWO));
    }

    /**
     * 逆数演算.
     * 速い?
     * @param a
     * @return 
     */
    public byte[] inv(byte[] a) {
        byte[] p = new byte[a.length];
        Arrays.fill(p, (byte) -1);
        p[p.length - 1]--;
        return pow(a, p);
    }

    /**
     * 逆数演算.
     * 速い?
     * @param a 
     * @return 
     */
    public long[] inv(long[] a) {
        long[] p = new long[a.length];
        Arrays.fill(p, -1l);
        p[p.length - 1]--;
        return pow(a, p);
    }

    /**
     * 累乗.
     *
     * @param a 底
     * @param p exponent 1以上 128bitではビット不足?
     * @return a^p mod xx
     */
    public byte[] pow(byte[] a, long p) {
        if (p == 1) {
            return a;
        } else {
            byte[] n;
            if (p % 3 == 0) {
                n = pow(a, p / 3);
                return mul(mul(n, n), n);
            }
            n = pow(a, p / 2);
            n = mul(n, n);
            if (p % 2 != 0) {
                n = mul(n, a);
//            Bin.xorl(n, a);
            }
            return n;
        }
    }

    /**
     * 累乗.
     *
     * @param a 底
     * @param p 指数
     * @return a^p
     */
    public long[] pow(long[] a, long p) {
        if (p == 1) {
            return a;
        } else {
            long[] n;
            if (p % 5 == 0) {
                n = pow(a, p / 5);
                long[] nn = mul(n, n);
                nn = mul(nn, nn);
                return mul(nn, n);
            }
            if (p % 3 == 0) {
                n = pow(a, p / 3);
                return mul(mul(n, n), n);
            }
            n = pow(a, p / 2);
            n = mul(n, n);
            if (p % 2 != 0) {
                n = mul(n, a);
            }
            return n;
        }
    }

    public byte[] pow(byte[] n, byte[] p) {
        byte[] x = new byte[n.length];
        x[x.length - 1] = 1;
        for (int i = 0; i < p.length; i++) {
            for (int j = 24; j < 32; j++) {
                x = mul(x, x);
                if ((p[i] << j) < 0) {
                    x = mul(x, n);
                }
            }
        }
        return x;
    }

    public long[] pow(long[] n, long[] p) {
        long[] x = new long[n.length];
        x[n.length - 1] = 1;
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < 64; j++) {
                x = mul(x, x);
                if ((p[i] << j) < 0) {
                    x = mul(x, n);
                }
            }
        }
        return x;
    }

    static final BigInteger SEVEN = BigInteger.valueOf(7);
    static final BigInteger eSEVEN = BigInteger.ONE.shiftLeft(128).subtract(TWO);

    /**
     * 簡易版 累乗.
     *
     * @param a 底
     * @param p 指数 exponent 1以上
     * @return a^p
     */
    public byte[] pow(byte[] a, BigInteger p) {
        if (p.equals(BigInteger.ONE)) {
            return a;
        } else {
            byte[] n;
            if (p.mod(THREE).equals(BigInteger.ZERO)) {
                n = pow(a, p.divide(THREE));
                return mul(mul(n, n), n);
            }
            n = pow(a, p.divide(TWO));
            n = mul(n, n);
            if (!p.mod(TWO).equals(BigInteger.ZERO)) {
                n = mul(n, a);
            }
            return n;
        }
    }

    /**
     * 累乗.
     *
     * @param a 底
     * @param p 指数 exponent
     * @return a^p
     */
    public long[] pow(long[] a, BigInteger p) {
        if (p.equals(BigInteger.ONE)) {
            return a;
        } else {
            long[] n;
            if (p.mod(FIVE).equals(BigInteger.ZERO)) {
                n = pow(a, p.divide(FIVE));
                long[] nn = mul(n, n);
                return mul(n, mul(nn, nn));
            }
            if (p.mod(THREE).equals(BigInteger.ZERO)) {
                n = pow(a, p.divide(THREE));
                return mul(n, mul(n, n));
            }
            n = pow(a, p.divide(TWO));
            n = mul(n, n);
            if (!p.mod(TWO).equals(BigInteger.ZERO)) {
                n = mul(n, a);
            }
            return n;
        }
    }

    public int mul(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        a &= size;
        b &= size;
        int e = log[a] + log[b];
        if (e >= size) {
            e -= size;
        }
        return exp[e];
    }

    public byte[] add(byte[] a, byte[] b) {
        return Bin.xor(a, b);
    }

    /**
     * 加算.　減算.
     * XOR
     *
     * @param a
     * @param b
     * @return a ＋ b
     */
    public long[] add(long[] a, long[] b) {
        return Bin.xor(a, b);
    }

    /**
     * ゼロ判定.
     *
     * @param a
     * @return aがゼロのとき true
     */
    private static boolean isZero(byte[] a) {
        for (byte c : a) {
            if (c != 0) return false;
        }
        return true;
    }

    private static boolean isZero(long[] a) {
        for (long c : a) {
            if (c != 0) return false;
        }
        return true;
    }

    /**
     * 積算.
     * a・b
     *
     * @param a
     * @param b
     * @return a・b
     */
    public byte[] mul(byte[] a, byte[] b) {
        if (a.length % 8 == 0) {
            return Bin.ltob(mul(Bin.btol(a), Bin.btol(b)));
        }
        byte[] r = new byte[a.length];
        if (isZero(b)) {
            return r;
        }
        /*
        int last = a.length - 1;
        while (!isZero(a)) {
            if ((a[last] & 0x01) != 0) {
                Bin.xorl(r, b);
            }
            a = Bin.shr(a);
            b = x(b);
        }
         */
        for (int i = a.length - 1; i >= 0; i--) {
            byte ai = a[i];
            if (ai == 0) {
                for (int j = 0; j < 8; j++) {
                    b = x(b);
                }
            } else {
                for (int j = 7; j >= 0; j--) {
                    if (((byte) (ai << j)) < 0) {
                        Bin.xorl(r, b);
                    }
                    b = x(b);
                }
            }
        }

        return r;
    }

    /**
     * 積算.
     * a・b
     *
     * @param a 整数
     * @param b 整数
     * @return a・b
     */
    public long[] mul(long[] a, long[] b) {
        long[] r = new long[a.length];
        if (isZero(b)) {
            return r;
        }
        int last = a.length - 1;
        while (!isZero(a)) {
            if ((a[last] & 1) != 0) {
                Bin.xorl(r, b);
            }
            a = Bin.shr(a);
            b = x(b);
        }
        return r;
    }

    public int div(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        int e = log[a] - log[b];
        if (e < 0) {
            e += size;
        }
        return exp[e];
    }

    /**
     * 割り算.
     * 逆数
     *
     * @param a
     * @param b
     * @return a / b
     */
    public byte[] div(byte[] a, byte[] b) {
        return mul(a, inv(b));
    }

    /**
     * 割り算.
     * 逆数
     *
     * @param a
     * @param b
     * @return a / b
     */
    public long[] div(long[] a, long[] b) {
        return mul(a, inv(b));
    }
}
