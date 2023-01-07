package net.siisise.math;

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

    // ガロア 有限体
//  final byte FF4 = 0x3; // 0x13 10011
    public static final byte FF8 = 0x1b; // 0x11b 100011011 AES
    public static final byte FF128 = (byte)0x87; // 0x10000000000000087 x10000111
    public static final byte[] GF8 = {FF8}; // 0x11b
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
        constRb = 0; // root側を使う
        size = (1 << n) - 1;
        x = new int[size + 1];
        log = new int[size + 1];
        exp = new int[size + 1];

        for (int a = 0; a <= size; a++) {
            x[a] = (a << 1) ^ ((a >>> N) * root);
        }

        int a = 1;
        for (int e = 0; e < size; e++) {
            log[a] = e;
            exp[e] = a;

            a ^= x(a);
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
        byte[] v = Bin.shl(s);
        if ((s[0] & 0x80) != 0) {
            v[v.length - 1] ^= constRb;
        }
        return v;
    }

    /**
     * long列 GF s・2
     * @param s 数
     * @return s・2
     */
    public long[] x(long[] s) {
        long[] v = Bin.shl(s);
        if ((s[0] & 0x8000000000000000l) != 0) {
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
        if ((s[0] & 0x01) != 0) {
            s[s.length - 1] ^= constRb;
        }
        return Bin.ror(s);
    }

    /**
     * a・2
     * @param a
     * @return a・2
     */
    public final int x(int a) {
//        return (a << 1) ^ ((a >>> N) * root); 
        return x[a];
    }

    public int inv(int a) {
        return a == 0 ? 0 : exp[size - log[a]];
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

/*
    public int mul(int x, int y) {
        if (x == 0 || y == 0) {
            return 0;
        }
        int m = 0;
    
        x &= size;
        y &= size;

        while (x > 0) {
            if ((x & 1) != 0) {
                m ^= y;
            }
            y = x(y);
            x >>>= 1;
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
        while ( !isZero(a) ) {
            if ( (a[a.length - 1] & 0x01) != 0 ) {
                r = Bin.xor(r, b);
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
        while ( !isZero(a) ) {
            if ( (a[a.length - 1] & 0x01) != 0 ) {
                r = Bin.xor(r, b);
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

    public static String toHexString(long[] s) {
        StringBuilder sb = new StringBuilder(32);
        for ( long v : s ) {
            String h = "000000000000000" + Long.toHexString(v);
            sb.append(h.substring(h.length() - 16));
        }
        return sb.toString();
    }
}
