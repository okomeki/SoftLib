package net.siisise.math;

import java.math.BigInteger;
import net.siisise.lang.Bin;

/**
 * ガロア体のなにか
 * 8bit用とbyte列,long列用
 */
public class GF {

    // 8bit - 32bit?
    final int N;   // 7
    final int root; // 0x11b
    final byte constRb; // root と同じ 
    int size; // 255

    final int[] x; // 計算済みのGF8
    final int[] log;
    final int[] exp;

    // ガロア 有限体 原始多項式?
//  final byte FF4 = 0x3; // 0x13 10011
    public static final byte FF8 = 0x1b; // 0x11b 100011011 AES
    public static final byte FF64  = 0x1b; // 0x1000000000000001b x11011
    public static final byte FF128 = (byte)0x87; // 0x100000000000000000000000000000087 x10000111 // GMAC
    public static final byte[] GF8 = {FF8}; // 0x11b
    public static final byte[] GF64 = {0,0,0,0,0,0,0,FF64}; // 0x1000000000000001b
    public static final byte[] GF128 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,FF128}; // 0x100000000000000000000000000000087 CMAC

    public GF() {
        this(8, 0x11b);
    }

    /**
     * 短い用 1バイトくらいの
     * @param n 2^n 8 bit を想定
     * @param m n = 8のとき 0x11bくらい 上のビットあり
     */
    public GF(int n, int m) {
        N = n - 1;
        root = m;
        constRb = (byte) root; // root側を使う
        size = (1 << n) - 1;
        x = new int[size + 1];
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
     * @param n ビット数 128を想定
     * @param rb 0x87 を想定
     */
    public GF(int n, byte[] rb) {
        this(n, rb[rb.length - 1]);
    }

    /**
     * 長い用
     * @param n ビット長 128bit
     * @param rb MSBを外したもの constやfinalなので複製しなくてもいい?
     */
    public GF(int n, byte rb) {
        N = n - 1;
        root = 0; // constRb 側をつかう
        constRb = rb;
        x = null;
        log = null;
        exp = null;
    }
    
    /**
     * ふつうのGF s・2
     * バイト数は未検証. てきとう.
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
     * @param s 数
     * @return s・2
     */
    public long[] x(long[] s) {
        long[] v = Bin.shl(s); // constRb に 1bit 持っているのでこっちは消す
        if (s[0] < 0) {
            v[v.length - 1] ^= constRb & 0xffl;
        }
        return v;
    }
    
    /**
     * GF s・2の逆 /2
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
     * @param a
     * @return a・2
     */
    public final int x(int a) {
        return (a << 1) ^ ((a >>> N) * root); 
//        return n[a];
    }
    
    public int r(int s) {
        return (s >>> 1) ^ ((s & 1) * root);
    }

    /**
     * 8bit 逆数計算的なもの(高速版)
     * @param a
     * @return aの逆数
     */
    public int inv(int a) {
        return a == 0 ? 0 : exp[size - log[a]];
    }
    
    static final BigInteger TWO = BigInteger.valueOf(2);
    static final BigInteger THREE = BigInteger.valueOf(3);
    
    /**
     * 逆数計算的なもの(簡易版)
     * 256 - 2 で ^254 ぐらいの位置づけ
     * ビット長*2回掛けるぐらいで計算はできる
     * @param a
     * @return aの逆数 60bit程度まで
     */
    public byte[] inv(byte[] a) {
        BigInteger p = TWO.shiftLeft(N).subtract(TWO);
        return pow(a, p);
//        return pow(a, (2l << N) - 2);
    }
    
    /**
     * あれ
     * @param a
     * @param p exponent 1以上
     * @return a^p mod xx
     */
    public byte[] pow(byte[] a, long p) {
        if ( p == 1 ) {
            return a;
        } else {
            byte[] n;
            if ( p % 3 == 0 ) {
                n = pow(a, p / 3 );
                return mul(mul(n,n),n);
            }
            n = pow(a, p / 2);
            n = mul(n, n);
            if ( p % 2 != 0 ) {
                n = mul(n, a);
//            Bin.xorl(n, a);
            }
            return n;
        }
    }

    public long[] pow(long[] a, long p) {
        if ( p == 1 ) {
            return a;
        } else {
            long[] n;
            if ( p % 3 == 0 ) {
                n = pow(a, p / 3 );
                return mul(mul(n,n),n);
            }
            n = pow(a, p / 2);
            n = mul(n, n);
            if ( p % 2 != 0 ) {
                n = mul(n, a);
//            Bin.xorl(n, a);
            }
            return n;
        }
    }
    
    /**
     * 簡易版
     * @param a 元
     * @param p exponent 1以上
     * @return 
     */
    public byte[] pow(byte[] a, BigInteger p) {
        if ( p.equals(BigInteger.ONE)) {
            return a;
        } else {
            byte[] n;
            if ( p.mod(THREE).equals(BigInteger.ZERO)) {
                n = pow(a, p.divide(THREE));
                return mul(mul(n,n),n);
            }
            n = pow( a, p.divide(TWO));
            n = mul(n,n);
            if ( !p.mod(TWO).equals(BigInteger.ZERO)) {
                n = mul(n,a);
            }
            return n;
        }
    }

//*    

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

    public long[] add(long[] a, long[] b) {
        return Bin.xor(a, b);
    }


/*
    public int mul(int n, int y) {
        if (n == 0 || y == 0) {
            return 0;
        }
        int m = 0;
    
        n &= size;
        y &= size;

        while (n > 0) {
            if ((n & 1) != 0) {
                m ^= y;
            }
            y = n(y);
            n >>>= 1;
        }
        return m;
    }
*/

    private static boolean isZero(byte[] a) {
        for ( byte c : a ) {
            if ( c != 0 ) return false;
        }
        return true;
    }

    private static boolean isZero(long[] a) {
        for ( long c : a ) {
            if ( c != 0 ) return false;
        }
        return true;
    }
    
    /**
     * a・b
     * @param a
     * @param b
     * @return a・b
     */
    public byte[] mul(byte[] a, byte[] b) {
        byte[] r = new byte[a.length];
        int last = a.length - 1;
        while ( !isZero(a) ) {
            if ( (a[last] & 0x01) != 0 ) {
                Bin.xorl(r, b);
            }
            a = Bin.shr(a);
            b = x(b);
        }
        return r;
    }

    /**
     * a・b
     * @param a
     * @param b
     * @return a・b
     */
    public long[] mul(long[] a, long[] b) {
        long[] r = new long[a.length];
        int last = a.length - 1;
        while ( !isZero(a) ) {
            if ( (a[last] & 0x01) != 0 ) {
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
     * @deprecated 未検証
     * @param a
     * @param b
     * @return a / b
     */
    @Deprecated
    public byte[] div(byte[] a, byte[] b) {
        return mul(a, inv(b));
    }

    public static String toHexString(long[] s) {
        StringBuilder sb = new StringBuilder(32);
        for ( long v : s ) {
            String h = "000000000000000" + Long.toHexString(v);
            sb.append(h.substring(h.length() - 16));
        }
        return sb.toString();
    }
}
