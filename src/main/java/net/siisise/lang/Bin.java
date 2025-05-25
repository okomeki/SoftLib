/*
 * Copyright 2022 Siisise Net.
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
package net.siisise.lang;

import java.math.BigInteger;
import java.util.Arrays;
import net.siisise.math.Matics;

/**
 * 名前候補 BinかHex
 * Hexというよりバイト列変換の主なもの.
 * 配列をintやlongとbyteで変換してみたり、ちょっとしたbit演算をまとめて実行するだけのまとまり.
 * HexとBASE64は統合したいかもしれない
 * Number べーす
 * java.util.BitSet があるらしいが変換には向かないので使わない.
 */
public class Bin {

    /**
     * バイト列を16進数(小文字)に変換する.
     *
     * @param src バイト列
     * @return 16進文字列
     */
    public static java.lang.String toHex(byte[] src) {
        char[] txt = new char[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            int d = (char) (src[i] & 0xff);
            int a = (d >> 4);
            a = a < 10 ? (a + '0') : (a + ('a' - 10));
            txt[i * 2] = (char) a;
            int b = d & 0xf;
            b = b < 10 ? (b + '0') : (b + ('a' - 10));
            txt[i * 2 + 1] = (char) b;
        }
        return new java.lang.String(txt);
    }

    /**
     * バイト列を16進数(大文字)に変換する.
     * BASE16互換
     *
     * @param src バイト列
     * @return 16進文字列(大文字)
     */
    public static java.lang.String toUpperHex(byte[] src) {
        char[] txt = new char[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            int d = (char) (src[i] & 0xff);
            int a = (d >> 4);
            a = a < 10 ? (a + '0') : (a + ('A' - 10));
            txt[i * 2] = (char) a;
            int b = d & 0xf;
            b = b < 10 ? (b + '0') : (b + ('A' - 10));
            txt[i * 2 + 1] = (char) b;
        }
        return new java.lang.String(txt);
    }

    /**
     * 16進数をバイト列に変換する。
     *
     * @param src 2の倍数の長さ限定 16進数文字列
     * @return バイト列
     */
    public static byte[] toByteArray(CharSequence src) {
        return toByteArray(src.toString().toCharArray());
    }

    /**
     * 16進数をバイト列に変換する。
     *
     * @param src 2の倍数の長さ限定 16進数文字列
     * @return バイト列
     */
    public static byte[] toByteArray(java.lang.String src) {
        return toByteArray(src.toCharArray(), 0, src.length());
    }

    /**
     * 16進数をバイト列に変換する。
     *
     * @param hex 2の倍数の長さ限定 16進数文字列
     * @return バイト列
     */
    public static byte[] toByteArray(char[] hex) {
        return toByteArray(hex, 0, hex.length);
    }

    /**
     * 16進数をバイト列に変換する。
     *
     * @param hex 2の倍数の長さ限定 16進数文字列
     * @param offset txt の16進数の位置
     * @param length txt の16進数の長さ
     * @return バイト列
     */
    public static byte[] toByteArray(char[] hex, int offset, int length) {
        int bit = 4;
        byte[] data = new byte[(length * bit + 7) / 8];
        int bd = 0;
        int bitlen = 0;
        int j = 0;
        for (int i = 0; i < length; i++) {
            int a = hex[offset + i];
            if (a >= '0' && a <= '9') {
                a -= '0';
            } else if (a >= 'a' && a <= 'f') {
                a -= 'a' - 10;
            } else if (a >= 'A' && a <= 'F') {
                a -= 'A' - 10;
            } else {
                throw new java.lang.IllegalStateException();
            }
            bd <<= bit;
            bd |= a;
            bitlen += bit;
            if (bitlen >= 8) {
                bitlen -= 8;
                data[j++] = (byte) (bd >> bitlen);
                bd &= (1 << bitlen) - 1;
            }
        }
        return data;
    }

    /**
     * n進数を変換する試み.
     * 2進数からHEX64まで対応したりしなかったり
     * BigIntegerでまわすのでスピードは期待しない方向で
     *
     * 36進数までは 0-9,a-z,A-Z
     * 62進数まではa-zとA-Zを区別する
     * 64進数までは記号をいくつか
     *
     * @param txt baae64まで
     * @param radixBase 6まで
     * @return
     */
    public static byte[] toByteArray(char[] txt, int radixBase) {
        int len = txt.length;
        BigInteger num = BigInteger.ZERO;
        BigInteger rad = BigInteger.valueOf(2).pow(radixBase);
        final int pad = 8 - radixBase;
        int b = 0;

        for (int i = 0; i < len; i++) {
            int a = txt[i];
            b += pad;
            b %= 8;
            if (a >= '0' && a <= '9') {
                a -= '0';
            } else if (a >= 'a' && a <= 'z') { // 10 - 35
                a -= 'a' - 10;
            } else if (a >= 'A' && a <= 'Z') { // 36 - 61
                if (radixBase > 5) { // てきとー
                    a -= 'A' - 36;
                } else {
                    a -= 'A' - 10;
                }
            } else if (a == '+' || a == '-') {
                a = 62;
            } else if (a == '/' || a == '_') {
                a = 63;
            } else {
                throw new java.lang.IllegalStateException();
            }
            num = num.multiply(rad).add(BigInteger.valueOf(a));
        }
        if (b > 0) {
            num = num.shiftRight(b);
        }

        return toByteArray(num, 1);
    }

    /**
     * new BigInteger(byte[]) の逆にしたい.
     *
     * @param num バイト列をBigInteger にしたもの
     * @param length 最短バイト長
     * @return 最短バイト長より長いこともある
     */
    public static byte[] toByteArray(BigInteger num, int length) {
        byte[] d = num.toByteArray();
        if (d.length >= length) {
            return d;
        }
        byte[] data = new byte[length];
        if (d[0] < 0) { // マイナスフラグ拡張
            Arrays.fill(data, 0, data.length - d.length, (byte) -1);
        }
        System.arraycopy(d, 0, data, data.length - d.length, d.length);
        return data;
    }

    /**
     * short を big endian 2バイトに変換する
     *
     * @param i 数値
     * @return 2バイト列
     */
    public static byte[] toByte(short i) {
        byte[] out = new byte[2];
        out[0] = (byte) (i >>> 8);
        out[1] = (byte) i;
        return out;
    }

    /**
     * short を out の offset位置から big endian 2バイトで書き込む.
     *
     * @param i 数値
     * @param out 書き込む先
     * @param offset 書き込み位置
     * @return outと同じ列
     */
    public static byte[] toByte(short i, byte[] out, int offset) {
        out[offset++] = (byte) (i >>> 8);
        out[offset] = (byte) i;
        return out;
    }

    /**
     * intを4バイトに変換する
     *
     * @param i 入力値
     * @return 4バイト列
     */
    public static byte[] toByte(int i) {
        return toByte(i, new byte[4], 0);
    }

    /**
     *
     * int を out の offset位置から big endian 4バイトで書き込む.
     *
     * @param i 数値
     * @param out 書き込む先
     * @param offset 書き込み位置
     * @return outと同じ列
     */
    public static byte[] toByte(int i, byte[] out, int offset) {
        out[offset++] = (byte) (i >>> 24);
        out[offset++] = (byte) (i >> 16);
        out[offset++] = (byte) (i >>> 8);
        out[offset] = (byte) i;
        return out;
    }

    /**
     * longをbig endian 8バイトに変換する
     *
     * @param l 入力値
     * @return 8バイト列
     */
    public static byte[] toByte(long l) {
        byte[] out = new byte[8];
        return toByte(l, out, 0);
    }

    /**
     * long を out の offset位置から big endian 8バイトで書き込む.
     *
     * @param l 数値
     * @param out 書き込む先
     * @param offset 書き込み位置
     * @return outと同じ列
     */
    public static byte[] toByte(long l, byte[] out, int offset) {
        out[0] = (byte) (l >>> 56);
        out[1] = (byte) (l >> 48);
        out[2] = (byte) (l >> 40);
        out[3] = (byte) (l >> 32);
        out[4] = (byte) (l >> 24);
        out[5] = (byte) (l >> 16);
        out[6] = (byte) (l >>> 8);
        out[7] = (byte) l;
        return out;
    }

    // Bin Byte系機能

    /**
     * 左詰めAND.
     * c = a AND b
     * returnでcとして返すタイプ
     *
     * @param a 長さの基準
     * @param aoffset aの開始位置
     * @param b
     * @param boffset bの開始位置
     * @param ret 長さの制限
     * @return a AND b 新配列
     */
    public static byte[] and(byte[] a, int aoffset, byte[] b, int boffset, byte[] ret) {
        int min = Matics.min(ret.length, a.length - aoffset, b.length - boffset);
        for (int i = 0; i < min; i++) {
            ret[i] = (byte) (a[aoffset + i] & b[boffset + i]);
        }
        if (min < ret.length) {
            Arrays.fill(ret, min, ret.length, (byte) 0);
        }
        return ret;
    }

    /**
     * aの長さ合わせAND.
     * bが短いときは0埋め.
     *
     * @param a
     * @param b
     * @return
     */
    public static byte[] and(byte[] a, byte[] b) {
        return and(a, 0, b, 0, new byte[a.length]);
    }

    /**
     * OR
     *
     * @param a 長さの基準
     * @param aoffset
     * @param b
     * @param boffset
     * @param ret
     * @return a OR b
     */
    public static byte[] or(byte[] a, int aoffset, byte[] b, int boffset, byte[] ret) {
        int min = Matics.min(ret.length, a.length - aoffset, b.length - boffset);
        for (int i = 0; i < min; i++) {
            ret[i] = (byte) (a[aoffset + i] | b[boffset + i]);
        }
        if (min < ret.length) {
            if (min < a.length - aoffset) {
                System.arraycopy(a, aoffset + min, ret, min, Math.min(ret.length, a.length - aoffset) - min);
            } else {
                System.arraycopy(b, boffset + min, ret, min, Math.min(ret.length, b.length - boffset) - min);
            }
        }
        return ret;
    }

    public static byte[] or(byte[] a, byte[] b) {
        return or(a, 0, b, 0, new byte[a.length]);
    }

    /**
     * ret = a XOR b
     *
     * @deprecated まだ未確定
     * @param a
     * @param aoffset
     * @param b
     * @param boffset
     * @param ret
     * @param roffset
     * @return a XOR b
     */
    @Deprecated
    public static byte[] xor(byte[] a, int aoffset, byte[] b, int boffset, byte[] ret, int roffset) {
        int min = Matics.min(ret.length - roffset, a.length - aoffset, b.length - boffset);
        for (int i = 0; i < min; i++) {
            ret[roffset + i] = (byte) (a[aoffset + i] ^ b[boffset + i]);
        }
        if (min < ret.length - roffset) {
            if (min < a.length - aoffset) {
                System.arraycopy(a, aoffset + min, ret, roffset + min, Math.min(ret.length, a.length - aoffset) - min);
            } else {
                System.arraycopy(b, boffset + min, ret, roffset + min, Math.min(ret.length, b.length - boffset) - min);
            }
        }
        return ret;
    }

    /**
     * XORを計算するよ
     *
     * @param a バイト列 長さの基準
     * @param b バイト列
     * @return a ^ b
     */
    public static byte[] xor(byte[] a, byte[] b) {
        return xor(a, 0, b, 0, new byte[a.length], 0);
    }

    /**
     * a と b の xor 計算を a に戻す
     *
     * @param a 結果もこっちへ
     * @param b
     * @return a = a ^ b
     */
    public static byte[] xorl(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            a[i] ^= b[i];
        }
        return a;
    }

    /**
     * long列のXOR
     *
     * @param a
     * @param b
     * @return a = a ^ b
     */
    public static long[] xorl(long[] a, long[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            a[i] ^= b[i];
        }
        return a;
    }

    /**
     * long[] XOR byte[]
     * a = x ^ b
     * @param a CBCのvector 的なもの 
     * @param b データ列
     * @param offset データ列位置
     * @param length aの長さ
     */
    public static final void xorl(long[] a, byte[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            for ( int j = 0, k = 56; j < 8; j++, k -= 8) {
                a[i] ^= ((((long)b[offset + i*8 + j]) & 0xff) << k);
            }
        }
    }

    /*
     * int[] XOR byte[]
     * a = x ^ b
     * @param a CBCのvector 的なもの 
     * @param b データ列
     * @param offset データ列位置
     * @param length aの長さ
     */
    public static final void xorl(int[] a, byte[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            for ( int j = 0, k = 24; j < 4; j++, k -= 8) {
                a[i] ^= ((((long)b[offset + i*8 + j]) & 0xff) << k);
            }
        }
    }

    /**
     * long[] XOR int[]
     * @param a long列a
     * @param b int列b
     * @param offset b位置
     * @param length a長さ
     */
    public static final void xorl(long[] a, int[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            int of = offset + i * 2;
            a[i] ^= (((long)b[of]) << 32) ^ (b[of + 1] & 0xffffffffl);
        }
    }

    /**
     * long[] XOR long[]
     * a ^= b
     * @param a long列a
     * @param b long列b
     * @param offset b位置
     * @param length a長さ
     */
    public static final void xorl(long[] a, long[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            a[i] ^= b[offset + i];
        }
    }

    /**
     * int[] XOR int[]
     * a ^= b
     * @param a long列a
     * @param b long列b
     * @param offset b位置
     * @param length a長さ
     */
    public static final void xorl(int[] a, int[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            a[i] ^= b[offset + i];
        }
    }

    /**
     * a と b の offset からの xor.
     *
     * @param a 入力a
     * @param aoffset aのバイト位置
     * @param b 入力b
     * @param boffset bのバイト位置
     * @param ret 結果格納用
     * @return retと同じ
     */
    public static long[] xor(long[] a, int aoffset, long[] b, int boffset, long[] ret) {
        int min = Matics.min(ret.length, a.length - aoffset, b.length - boffset);
        for (int i = 0; i < min; i++) {
            ret[i] = a[aoffset + i] ^ b[boffset + i];
        }
        if (min < ret.length) {
            if (min < a.length - aoffset) {
                System.arraycopy(a, aoffset + min, ret, min, Math.min(ret.length, a.length - aoffset) - min);
            } else {
                System.arraycopy(b, boffset + min, ret, min, Math.min(ret.length, b.length - boffset) - min);
            }
        }
        return ret;
    }

    /**
     * aとbのxorを返す.
     *
     * @param a 入力a
     * @param b 入力b
     * @return xor結果, 長さはaと同じ
     */
    public static long[] xor(long[] a, long[] b) {
        return xor(a, 0, b, 0, new long[a.length]);
    }

    public static byte[] not(byte[] a, int aoffset, byte[] ret) {
        int min = Matics.min(ret.length, a.length - aoffset);
        for (int i = 0; i < min; i++) {
            ret[i] = (byte) (~a[aoffset + i]);
        }
        if (min < ret.length) {
            Arrays.fill(ret, min, ret.length, (byte) 0xff);
        }
        return ret;
    }

    public static byte[] not(byte[] a) {
        return not(a, 0, new byte[a.length]);
    }

    /**
     * Big endian 左シフト
     *
     * @param a 元列
     * @return 1ビット左シフト
     */
    public static byte[] shl(byte[] a) {
        byte[] n = new byte[a.length];
        int v = Byte.toUnsignedInt(a[0]);
        for (int i = 1; i < a.length; i++) {
            v = (v << 8) | Byte.toUnsignedInt(a[i]);
            n[i - 1] = (byte) (v >>> 7);
        }
        n[a.length - 1] = (byte) (v << 1);
        return n;
    }

    /**
     * Big endian 左シフト
     *
     * @param a 元列
     * @return シフトした列
     */
    public static long[] shl(long[] a) {
        long[] n = new long[a.length];
        for (int i = 0; i < a.length - 1; i++) {
            n[i] = (a[i] << 1) | (a[i + 1] >>> 63);
        }
        n[a.length - 1] = a[a.length - 1] << 1;
        return n;
    }

    public static byte[] rol(byte[] a) {
        int b = Byte.toUnsignedInt(a[0]) >> 7;
        byte[] n = shl(a);
        n[a.length - 1] |= b;
        return n;
    }

    /**
     * MSB が 配列 0 側にある想定のシフト演算
     *
     * @param a 配列
     * @param shift シフトビット数 とりあえず 0 から 7
     * @return シフトされた列
     */
    public static byte[] left(byte[] a, int shift) {
        byte[] b = new byte[a.length];
        int v = Byte.toUnsignedInt(a[0]);
        for (int i = 1; i < a.length; i++) {
            v = (v << 8) | Byte.toUnsignedInt(a[i]);
            b[i - 1] |= (byte) (v >>> (8 - shift));
        }
        b[a.length - 1] = (byte) (v << shift);
        return b;
    }

    /**
     * 右1bitシフト
     *
     * @param a 元列
     * @return 右シフト
     */
    public static byte[] shr(final byte[] a) {
        byte[] n = new byte[a.length];
        for (int i = a.length - 1; i > 0; i--) {
            n[i] = (byte) ((Byte.toUnsignedInt(a[i]) >> 1) | (Byte.toUnsignedInt(a[i - 1]) << 7));
        }
        n[0] = (byte) (Byte.toUnsignedInt(a[0]) >> 1);
        return n;
    }

    /**
     * 右1bitシフト
     *
     * @param a 非破壊
     * @return 右シフト
     */
    public static int[] shr(final int[] a) {
        int[] n = new int[a.length];
        for (int i = a.length - 1; i > 0; i--) {
            n[i] = (a[i] >>> 1) | (a[i - 1] << 31);
        }
        n[0] = a[0] >>> 1;
        return n;
    }

    /**
     * 右1bitシフト
     *
     * @param a 非破壊
     * @return 右シフト
     */
    public static long[] shr(final long[] a) {
        long[] n = new long[a.length];
        for (int i = a.length - 1; i > 0; i--) {
            n[i] = (a[i] >>> 1) | (a[i - 1] << 63);
        }
        n[0] = a[0] >>> 1;
        return n;
    }

    /**
     * 右1bit rotate
     *
     * @param a 非破壊
     * @return
     */
    public static byte[] ror(byte[] a) {
        byte b = (byte) (a[a.length - 1] << 7);
        byte[] n = shr(a);
        n[0] |= b;
        return n;
    }

    /**
     * 右1bit rotate
     *
     * @param a 非破壊
     * @return
     */
    public static int[] ror(int[] a) {
        int b = a[a.length - 1] << 31;
        int[] n = shr(a);
        n[0] |= b;
        return n;
    }

    /**
     *
     * @param a 非破壊
     * @return
     */
    public static long[] ror(long[] a) {
        long b = a[a.length - 1] << 63;
        long[] n = shr(a);
        n[0] |= b;
        return n;
    }

    public static byte[] right(byte[] a, int shift) {
        byte[] n = new byte[a.length];
        int v = Byte.toUnsignedInt(a[a.length - 1]) << 8;
        for (int i = a.length - 2; i >= 0; i--) {
            v = (Byte.toUnsignedInt(a[i]) << 8) | (v >> 8);
            n[i + 1] = (byte) (v >>> shift);
        }
        n[0] = (byte) (v >>> (shift + 8));
        return n;
    }

    /**
     * byte[]をint[]に変換する.
     *
     * @param src byte列
     * @return int列
     */
    public static final int[] btoi(final byte[] src) {
        int t = 0;
        int dl = src.length / 4;
        int[] dst = new int[dl];
        for (int i = 0; i < dst.length; i++, t += 4) {
            dst[i]
                    =  (src[t    ]         << 24)
                    | ((src[t + 1] & 0xff) << 16)
                    | ((src[t + 2] & 0xff) <<  8)
                    |  (src[t + 3] & 0xff);
        }
        return dst;
    }

    /**
     * byte[]をlong[]に変換する.
     *
     * @param src byte列
     * @return long列
     */
    public static final long[] btol(final byte[] src) {
        int t = 0;
        long[] dst = new long[src.length / 8];
        for (int i = 0; i < dst.length; i++, t += 8) {
            dst[i]
                    = ( ((long) src[t    ])         << 56)
                    | ((((long) src[t + 1]) & 0xff) << 48)
                    | ((((long) src[t + 2]) & 0xff) << 40)
                    | ((((long) src[t + 3]) & 0xff) << 32)
                    | ((((long) src[t + 4]) & 0xff) << 24)
                    | ((((long) src[t + 5]) & 0xff) << 16)
                    | ((((long) src[t + 6]) & 0xff) << 8)
                    |  (((long) src[t + 7]) & 0xff);
        }
        return dst;
    }

    /**
     * int列をbyte列に変換する.
     *
     * @param src int配列
     * @return byte配列
     */
    public static byte[] itob(final int[] src) {
        byte[] ss = new byte[src.length * 4];
        for (int i = 0; i < src.length; i++) {
            int l = i * 4;
            ss[l++] = (byte) (src[i] >> 24);
            ss[l++] = (byte) (src[i] >> 16);
            ss[l++] = (byte) (src[i] >>  8);
            ss[l  ] = (byte)  src[i]       ;
        }
        return ss;
    }

    /**
     * Little Endian int列をbyte列に変換する.
     *
     * @param src int配列
     * @return byte配列
     */
    public static byte[] litob(final int[] src) {
        byte[] ss = new byte[src.length * 4];
        for (int i = 0; i < src.length; i++) {
            int l = i * 4;
            ss[l++] = (byte)  src[i]       ;
            ss[l++] = (byte) (src[i] >>  8);
            ss[l++] = (byte) (src[i] >> 16);
            ss[l  ] = (byte) (src[i] >> 24);
        }
        return ss;
    }

    /**
     * long列をbyte列に変換する
     *
     * @param src long列
     * @return byte列
     */
    public static byte[] ltob(final long[] src) {
        byte[] ds = new byte[src.length * 8];
        for (int i = 0; i < src.length; i++) {
            long s = src[i];
            int l = i * 8;
            ds[l    ] = (byte) (s >> 56);
            ds[l + 1] = (byte) (s >> 48);
            ds[l + 2] = (byte) (s >> 40);
            ds[l + 3] = (byte) (s >> 32);
            ds[l + 4] = (byte) (s >> 24);
            ds[l + 5] = (byte) (s >> 16);
            ds[l + 6] = (byte) (s >>  8);
            ds[l + 7] = (byte) (s      );
        }
        return ds;
    }

    /**
     * Little Endian long列をbyte列に変換する
     *
     * @param src long列
     * @return byte列
     */
    public static byte[] lltob(final long[] src) {
        byte[] ds = new byte[src.length * 8];
        for (int i = 0; i < src.length; i++) {
            long s = src[i];
            int l = i * 8;
            ds[l    ] = (byte) (s      );
            ds[l + 1] = (byte) (s >>  8);
            ds[l + 2] = (byte) (s >> 16);
            ds[l + 3] = (byte) (s >> 24);
            ds[l + 4] = (byte) (s >> 32);
            ds[l + 5] = (byte) (s >> 40);
            ds[l + 6] = (byte) (s >> 48);
            ds[l + 7] = (byte) (s >> 56);
        }
        return ds;
    }

    /**
     * long列をint列に変換する
     *
     * @param src long列
     * @return int列
     */
    public static final int[] ltoi(final long[] src) {
        int[] ss = new int[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            int l = i * 2;
            ss[l + 1] = (int) src[i];
            ss[l] = (int) (src[i] >> 32);
        }
        return ss;
    }

    /**
     * int列をlong列に変換する
     *
     * @param src int列
     * @return long列
     */
    public static final long[] itol(final int[] src) {
        long[] ss = new long[src.length / 2];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = (((long) src[i * 2]) << 32)
                    | (((long) src[i * 2 + 1]) & 0xffffffffl);
        }
        return ss;
    }

    /**
     * int列の一部をbyte列に変換する
     *
     * @param src int列
     * @param offset 位置
     * @param len 長さ
     * @return byte列
     */
    public static final byte[] itob(final int[] src, int offset, int len) {
        byte[] ss = new byte[len * 4];
        int l = 0;
        for (int i = len; i > 0; i--) {
            int v = src[offset++];
            ss[l++] = (byte) (v >> 24);
            ss[l++] = (byte) (v >> 16);
            ss[l++] = (byte) (v >>  8);
            ss[l++] = (byte)  v       ;
        }
        return ss;
    }

    /**
     * byte列の一部をint列に変換する
     *
     * @param src byte列
     * @param offset 位置
     * @param length 長さ
     * @return int列
     */
    public static final int[] btoi(final byte[] src, int offset, int length) {
        int[] dst = new int[length];
        for (int i = 0; i < dst.length; i++) {
            dst[i]
                    = ( src[offset]             << 24)
                    | ((src[offset + 1] & 0xff) << 16)
                    | ((src[offset + 2] & 0xff) <<  8)
                    |  (src[offset + 3] & 0xff);
            offset += 4;
        }
        return dst;
    }

    /**
     * Little Endian っぽい
     * @param src byte列
     * @param offset 位置
     * @param length 長さ
     * @return int列
     */
    public static final int[] btoli(final byte[] src, int offset, int length) {
        int[] dst = new int[length];
        for (int i = 0; i < dst.length; i++) {
            dst[i]
                    =  (src[offset]     & 0xff)                  
                    | ((src[offset + 1] & 0xff) <<  8)
                    | ((src[offset + 2] & 0xff) << 16)
                    | ( src[offset + 3]         << 24);
            offset += 4;
        }
        return dst;
    }

    /**
     * byte列の一部をlong列に変換する
     *
     * @param src byte列
     * @param offset 位置
     * @param length 長さ
     * @return long列
     */
    public static final long[] btol(final byte[] src, int offset, int length) {
        long[] dst = new long[length];
        for (int i = 0; i < dst.length; i++, offset += 8) {
            dst[i]
                    = (((long)  src[offset    ]        ) << 56)
                    | (((long) (src[offset + 1] & 0xff)) << 48)
                    | (((long) (src[offset + 2] & 0xff)) << 40)
                    | (((long) (src[offset + 3] & 0xff)) << 32)
                    | (((long) (src[offset + 4] & 0xff)) << 24)
                    | (((long) (src[offset + 5] & 0xff)) << 16)
                    | (((long) (src[offset + 6] & 0xff)) <<  8)
                    |  ((long) (src[offset + 7] & 0xff));
        }
        return dst;
    }

    /**
     * long列の一部をint列に変換する
     *
     * @param src long列
     * @param offset src位置
     * @param length src長さ
     * @return int列
     */
    public static final int[] ltoi(final long[] src, int offset, int length) {
        int[] dst = new int[length];
        for (int i = 0; i < dst.length; i += 2) {
            dst[i    ] = (int) (src[offset] >> 32);
            dst[i + 1] = (int) (src[offset++] & 0xffffffffl);
        }
        return dst;
    }

    /**
     * long列の一部をbyte列に変換する
     *
     * @param src long列
     * @param offset src位置
     * @param srclen src長さ
     * @return byte列
     */
    public static byte[] ltob(final long[] src, int offset, int srclen) {
        byte[] ss = new byte[srclen * 8];
        int l = 0;
        for (int i = srclen; i > 0; i--) {
            long v = src[offset++];
            ss[l    ] = (byte) (v >> 56);
            ss[l + 1] = (byte) (v >> 48);
            ss[l + 2] = (byte) (v >> 40);
            ss[l + 3] = (byte) (v >> 32);
            ss[l + 4] = (byte) (v >> 24);
            ss[l + 5] = (byte) (v >> 16);
            ss[l + 6] = (byte) (v >>  8);
            ss[l + 7] = (byte)  v       ;
            l += 8;
        }
        return ss;
    }

    /**
     * byte列の一部をlong列に変換する
     *
     * @param src 転送元byte列
     * @param offset 転送元位置
     * @param dst 転送先long列
     * @param length 転送先長
     */
    public static final void btol(final byte[] src, int offset, long[] dst, int length) {
        for (int i = 0; i < length; i++, offset += 8) {
            dst[i]
                    = ( (long) src[offset    ]         << 56)
                    | (((long) src[offset + 1] & 0xff) << 48)
                    | (((long) src[offset + 2] & 0xff) << 40)
                    | (((long) src[offset + 3] & 0xff) << 32)
                    | (((long) src[offset + 4] & 0xff) << 24)
                    | (((long) src[offset + 5] & 0xff) << 16)
                    | (((long) src[offset + 6] & 0xff) <<  8)
                    |  ((long) src[offset + 7] & 0xff);
        }
    }

    /**
     * byte列の一部をlong列に変換する
     *
     * @param src byte列
     * @param offset src位置
     * @param dst long列
     * @param length 長さ
     */
    public static final void itol(final int[] src, int offset, long[] dst, int length) {
        for (int i = 0; i < length; i++) {
            dst[i]
                    = (((long) src[offset]) << 32)
                    | ((long) src[offset + 1] & 0xffffffffl);
            offset += 2;
        }
    }

    /**
     * byte[]をint[]に変換する.
     *
     * @param src バイト列
     * @param offset src位置
     * @param dst 戻りint列
     * @param length int長
     */
    public static void btoi(final byte[] src, int offset, int[] dst, int length) {
        for (int i = 0; i < length; i++) {
            dst[i]
                    = ( src[offset    ]         << 24)
                    | ((src[offset + 1] & 0xff) << 16)
                    | ((src[offset + 2] & 0xff) <<  8)
                    |  (src[offset + 3] & 0xff);
            offset += 4;
        }
    }

    public static void btoli(final byte[] src, int offset, int[] dst, int length) {
        for (int i = 0; i < length; i++) {
            dst[i]
                    =  (src[offset    ] & 0xff)      
                    | ((src[offset + 1] & 0xff) <<  8)
                    | ((src[offset + 2] & 0xff) << 16)
                    |  (src[offset + 3]         << 24); 
            offset += 4;
        }
    }

    /**
     * long列の一部をint列に変換する
     *
     * @param src long列
     * @param offset src位置
     * @param dst 戻りint列
     * @param length 長さ
     */
    public static void ltoi(final long[] src, int offset, int[] dst, int length) {
        for (int i = 0; i < length; i += 2) {
            dst[i    ] = (int) (src[offset] >> 32);
            dst[i + 1] = (int) (src[offset++] & 0xffffffffl);
        }
    }

    /**
     * int[]をbyte[]に戻す.
     *
     * @param src
     * @param ss
     * @param doffset
     * @return
     */
    public static byte[] itob(final int[] src, byte[] ss, int doffset) {
        for (int i = 0; i < src.length; i++) {
            int l = doffset + i * 4;
            ss[l++] = (byte) (src[i] >> 24);
            ss[l++] = (byte) (src[i] >> 16);
            ss[l++] = (byte) (src[i] >>  8);
            ss[l  ] = (byte)  src[i]       ;
        }
        return ss;
    }

    /**
     * Little Endian int[]をbyte[]に戻す.
     *
     * @param src
     * @param ss
     * @param doffset
     * @return
     */
    public static byte[] litob(final int[] src, byte[] ss, int doffset) {
        for (int i = 0; i < src.length; i++) {
            int l = doffset + i * 4;
            ss[l++] = (byte)  src[i]       ;
            ss[l++] = (byte) (src[i] >>  8);
            ss[l++] = (byte) (src[i] >> 16);
            ss[l  ] = (byte) (src[i] >> 24);
        }
        return ss;
    }

    /**
     * long[] to byte[].
     *
     * @param src
     * @param ss
     * @param doffset
     * @return
     */
    public static final byte[] ltob(final long[] src, byte[] ss, int doffset) {
        for (int i = 0; i < src.length; i++) {
            int l = doffset + i * 8;
            ss[l    ] = (byte) (src[i] >> 56);
            ss[l + 1] = (byte) (src[i] >> 48);
            ss[l + 2] = (byte) (src[i] >> 40);
            ss[l + 3] = (byte) (src[i] >> 32);
            ss[l + 4] = (byte) (src[i] >> 24);
            ss[l + 5] = (byte) (src[i] >> 16);
            ss[l + 6] = (byte) (src[i] >>  8);
            ss[l + 7] = (byte)  src[i]       ;
        }
        return ss;
    }

    /**
     * byte列を大きな数値として+1する
     *
     * @param src 数値1つぶん
     */
    public static final void inc(byte[] src) {
        int x = src.length;
        do {
            x--;
            src[x]++;
        } while (src[x] == 0 && x > 0);
    }

    /**
     * long列を大きな数値として+1する
     *
     * @param src 数値1つぶん
     */
    public static final void inc(long[] src) {
        int x = src.length;
        do {
            x--;
            src[x]++;
        } while (src[x] == 0 && x > 0);
    }
}
