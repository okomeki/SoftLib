package net.siisise.math;

import net.siisise.lang.Bin;

/**
 * ガロア体のなにか
 */
public class GF {

    final int N;   // 7
    final int root; // 0x11b
    final byte[] constRb; // root と同じ 
    int size; // 255

    final int[] x; // 計算済みのGF8
    final int[] log;
    final int[] exp;

    // ガロア 有限体
//  final byte FF4 = 0x3; // 0x13 10011
    static final byte FF8 = 0x1b; // 0x11b 100011011 AES
    static final byte FF128 = (byte)0x87; // 0x10000000000000087 x10000111
    static final byte[] GF8 = {FF8}; // 0x11b
    public static final byte[] GF128 = {0,0,0,0,0,0,0,FF128}; // 0x10000000000000087 CMAC

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
        constRb = null; // root側を使う
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
     * 長い用
     * @param n 128
     * @param rb MSBを外したもの constやfinalなので複製しなくてもいい?
     */
    public GF(int n, byte[] rb) {
        N = n - 1;
//        size = (1 << n) - 1; // 使わない
        root = 0; // constRb 側をつかう
        constRb = rb;
        x = null;
        log = null;
        exp = null;
    }

    /**
     * ふつうのGF *2
     * @param s 数
     * @return s・2
     */
    public byte[] x(byte[] s) {
        byte[] v = Bin.shl(s);
        if ((s[0] & 0x80) != 0) {
            v = Bin.xor(v, constRb);
        }
        return v;
    }

    /**
     * GFの逆 /2
     * @param s
     * @return 
     */
    public byte[] r(byte[] s) {
        if ((s[0] & 0x01) != 0) {
            s = Bin.xor(s, constRb);
        }
        return Bin.ror(s);
    }

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

    private boolean isZero(byte[] a) {
        for ( byte c : a ) {
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

}
