/*
 * The MIT License
 *
 * Copyright 2024 okome.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.siisise.math;

import java.math.BigInteger;
import net.siisise.lang.Bin;

/**
 * GF Little Bit Endian.
 * GF 反転版.
 * GCMが反転していたので作ってみる.
 * a・L
 * ・L 用キャッシュをもたせて高速化
 */
public class GFRev {
    public static final long RGF128 = 0xe100000000000000l;

    private final long[][] shL;
    long constRb = RGF128;

    /**
     * 
     * @param l  掛ける値
     */
    public GFRev(long[] l) {
        
        shL = new long[l.length * 64][];
        long[] n;
        n = shL[0] = l.clone();
        for (int i = 1; i < l.length * 64; i++) {
            n = shL[i] = x(n);
        }
    }

    /**
     * a・2
     * @param a
     * @return a・2
     */
    public long[] x(long[] a) {
        long[] v = Bin.shr(a); // constRb に 1bit 持っているのでこっちは消す
        v[0] ^= constRb * (a[a.length - 1] & 1);
        return v;
    }

    /**
     * 高速積算.
     * a・b
     * Lのシフトビットをキャッシュしておくことで高速化したもの.
     * @param b
     * @return a・b
     */
    public long[] mul(long[] b) {
        long[] v = new long[b.length];
        int k = -1;
        for (int i = 0; i < b.length; i++) {
            long c = b[i];
            for ( int j = 0; j < 64; j++ ) {
                k++;
                if ( c << j < 0) {
                    Bin.xorl(v, shL[k]);
                }
            }
        }
        return v;
    }

    public long[] mul(long[] a, long[] b) {
        long[] v = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            long c = a[i];
            for ( int j = 0; j < 64; j++ ) {
                if ( c << j < 0) {
                    Bin.xorl(v, b);
                }
                b = x(b);
            }
        }
        return v;
    }

    /**
     * 加算。減算.
     * a + b
     * @param a
     * @param b
     * @return 
     */
    public long[] add(long[] a, long[] b) {
        return Bin.xor(a, b);
    }
    
    static final BigInteger TWO = BigInteger.valueOf(2);
    static final BigInteger THREE = BigInteger.valueOf(3);

//    static final BigInteger INV_POW = BigInteger.ONE.shiftLeft(128).subtract(TWO);
    static final long[] INV_LONG_POW = {0xffffffffffffffffl, 0xfffffffffffffffdl};

    /**
     * 逆数計算.
     * 
     * @param a 値
     * @return 逆数
     */
    public long[] inv(long[] a) {
        return new GFRev(a).pow(INV_LONG_POW);
    }
    
    public long[] inv() {
        return pow(INV_LONG_POW);
    }
    
    /**
     * 累乗.
     * @param a 底
     * @param p exponent 1以上
     * @return a^p
     */
    public long[] pow(long[] a, BigInteger p) {
        return new GFRev(a).pow(p);
/*
        if ( p.equals(BigInteger.ONE)) {
            return a;
        } else {
            long[] n;
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
*/
    }
    
    public long[] pow(long[] p) {
        long[] n = shL[0];
        long[] x = {0,1};
        for ( int i = 0; i < p.length; i++) {
            for ( int j = 0; j < 64; j++) {
                if ( (p[i] << j) < 0) {
                    x = mul(x, n);
                }
                x = mul(x, x);
            }
        }
        return x;
    }

    BigInteger OCT = BigInteger.valueOf(256);

    public long[] pow(BigInteger p) {
        if ( p.equals(BigInteger.ONE)) {
            return shL[0];
        } else {
            long[] n;
            if ( p.compareTo(OCT) > 0 ) {
                BigInteger m = p.mod(OCT);
                n = pow(p.divide(OCT));
                for ( int i = 0; i < 8; i++ ) {
                    n = mul(n,n);
                }
                if ( !m.equals(BigInteger.ZERO)) {
                    n = mul( n, pow(p));
                }
                return n;
            }
            if ( p.mod(THREE).equals(BigInteger.ZERO)) {
                n = pow(p.divide(THREE));
                GFRev gfn = new GFRev(n);
                return gfn.mul(gfn.mul(n));
            }
            n = pow( p.divide(TWO));
            n = mul(n,n);
            if ( !p.mod(TWO).equals(BigInteger.ZERO)) {
                n = mul(n);
            }
            return n;
        }
    }

}
