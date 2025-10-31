/*
 * Copyright 2025 okome.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.siisise.math;

import java.math.BigInteger;
import java.util.Arrays;
import net.siisise.lang.Bin;

/**
 * 128bit以上用 ガロア拡大体らしきもの.
 * 使える条件 p[0]が 1bitのみ1であること
 * Galois Field
 * 有限体
 *
 */
public class GFL {

    // 互換テスト用
    public static final long[] GF128 = {1, 0, 0x87};

    final int preBit;
    long mask;
    final long[] p;
    final long[] rp;
    BigInteger n;

    // 片側
//    private final long[][] shL;
    private long flg;
    private int findex;

    //
    long[] a;

    /**
     * 固定サイズ.
     *
     * @param rb p
     */
    public GFL(long rb) {
        this(new long[]{1, 0, rb});
    }

    public GFL(BigInteger p) {
        this(p.toByteArray());
    }

    public GFL(byte[] bp) {
        this(toLong(bp));
    }

    public GFL(long[] b) {
        // 列詰め
        for (int i = 0; i < b.length; i++) {
            if (b[i] != 0) {
                b = Arrays.copyOfRange(b, i, b.length);
                break;
            }
        }
        long[] rs = Bin.shr(b);
        int bb = 0;
        if (rs[0] == 0) { // b[0] == 1
            rs = Arrays.copyOfRange(rs, 1, rs.length);
            b = Arrays.copyOfRange(b, 1, b.length);
            preBit = 63;
            rs[0] |= 0x8000000000000000l;
            rp = rs;
        } else {
            long f = 0x4000000000000000l;
            long t = b[0];
            for (int i = 1; i < 64; i++) {
                if ((t & f) != 0) {
                    bb = i;
                    break;
                }
                f >>>= 1;
            }
            preBit = (62 - bb) & 63;
            rp = rs;
        }
        p = b;
        mask = 1l << preBit;
    }

    public GFL(long[] a, long[] b) {
        this(b);
        this.a = a.clone();
    }

    /**
     *
     * @param b
     * @return
     */
    public static long[] toLong(byte[] b) {
        long[] l = new long[(b.length + 7) / 8];
        int e = l.length * 8 - b.length;
        for (int o = l.length * 8 - 1; o >= e; o--) {
            l[o / 8] |= (b[o - e] & 0xffl) << ((7 - (o % 8)) * 8);
        }
        return l;
    }

    /**
     * 逆数演算.
     * 速い?
     *
     * @param a
     * @return
     */
    public long[] inv(long[] a) {
        long[] pp = new long[a.length];

        Arrays.fill(pp, -1l);
        pp[0] = mask * 2 - 1;
        pp[pp.length - 1]--;
        return pow(a, pp);
    }

    /*
    public long[] inv(long[] a) {
        BigInteger n = BigInteger.ONE.shiftLeft(bit).subtract(BigInteger.TWO);
        return pow(a, n);
    }
     */
    /**
     * 左シフト演算っぽい動作 1つ.
     * s・2.
     * 名前は未定.
     *
     * @param s
     * @return s・2
     */
    public final long[] x(long[] s) {
        long[] v = Bin.shl(s);
        if ((s[0] & mask) != 0) {
            Bin.xorl(v, p);
        }
        return v;
    }

    /**
     * 右シフト演算っぽい動作.
     * s/2.
     * 名前は未定.
     *
     * @param s
     * @return
     */
    public long[] r(long[] s) {
        long[] v = Bin.shr(s); // constRbの1bit が消えるのでrorで付ける
        if ((s[s.length - 1] & 1) != 0) {
            v = Bin.xor(v, rp);
        }
        return v;
    }

    public long[] add(long[] a, long[] b) {
        return Bin.xor(a, b);
    }

    /**
     * 積算.
     * this x b
     *
     * @param b 対象b
     * @return this x b
     */
    public long[] mul(long[] b) {
        return mul(a, b);
    }

    /**
     * a・b 積算.
     *
     * @param a a
     * @param b b
     * @return a・b
     */
    public long[] mul(long[] a, long[] b) {
        long[] r = new long[b.length];
        if (!isZero(b)) {
            int last = a.length - 1;
            while (!isZero(a)) {
                if ((a[last] & 1) != 0) {
                    Bin.xorl(r, b);
                }
                a = Bin.shr(a);
                b = x(b);
            }
        }
        return r;
    }

    /**
     * 積算.
     *
     * @param a
     * @param b
     * @return a・b
     */
    public byte[] mul(byte[] a, byte[] b) {
        return Bin.ltob(mul(toLong(a), toLong(b)));
    }

    /**
     * べき乗 a^n
     *
     * @param a
     * @param n べき
     * @return a^n
     */
    public long[] pow(long[] a, long[] n) {
        long[] x = new long[a.length];
        x[x.length - 1] = 1;
        int st = preBit;
        for (int i = 0; i < n.length; i++) {
            long ni = n[i];
            for (int j = st; j >= 0; j--) {
                x = mul(x, x);
                if ((ni & (1l << j)) != 0) {
                    x = mul(x, a);
                }
            }
            st = 63;
        }
        return x;
    }

    static boolean isZero(long[] a) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != 0) {
                return false;
            }
        }
        return true;
    }
}
