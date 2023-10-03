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
 * HexとBASE64は統合したいかもしれない
 * Number べーす
 * java.util.BitSet があるらしい 
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

    public static byte[] toByteArray(char[] txt) {
        return toByteArray(txt, 0, txt.length);
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
            if ( bitlen >= 8 ) {
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
                if ( radixBase > 5) { // てきとー
                    a -= 'A' - 36;
                } else {
                    a -= 'A' - 10;
                }
            } else if ( a == '+' || a == '-') {
                a = 62;
            } else if ( a == '/' || a == '_') {
                a = 63;
            } else {
                throw new java.lang.IllegalStateException();
            }
            num = num.multiply(rad).add(BigInteger.valueOf(a));
        }
        if ( b > 0 ) {
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
        if ( d.length >= length ) return d;
        byte[] data = new byte[length];
        if ( d[0] < 0 ) { // マイナスフラグ拡張
            Arrays.fill(data, 0, data.length - d.length, (byte)-1);
        }
        System.arraycopy(d, 0, data, data.length - d.length , d.length);
        return data;
    }
    
    /**
     * short を big endian 2バイトに変換する
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
     * @param i 入力値
     * @return 4バイト列
     */
    public static byte[] toByte(int i) {
        return toByte(i, new byte[4], 0);
    }

    /**
     * 
     * int を out の offset位置から big endian 4バイトで書き込む.
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
     * @param l 入力値
     * @return 8バイト列
     */
    public static byte[] toByte(long l) {
        byte[] out = new byte[8];
        return toByte(l, out, 0);
    }

    /**
     * long を out の offset位置から big endian 8バイトで書き込む.
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
     * AND
     * 左詰め
     * c = a AND b
     * returnでcとして返すタイプ
     * @param a 長さの基準
     * @param aoffset aの開始位置
     * @param b
     * @param boffset bの開始位置
     * @param ret 長さの制限
     * @return a AND b 新配列
     */
    public static byte[] and(byte[] a, int aoffset, byte[] b, int boffset, byte[] ret ) {
        int min = Matics.min( ret.length, a.length - aoffset, b.length - boffset );
        for ( int i = 0; i < min; i++ ) {
            ret[i] = (byte)(a[aoffset + i] & b[boffset + i]);
        }
        if ( min < ret.length ) {
            Arrays.fill(ret, min, ret.length, (byte)0);
        }
        return ret;
    }

    public static byte[] and(byte[] a, byte[] b) {
        return and(a, 0, b, 0, new byte[a.length]);
    }

    /**
     * OR
     * @param a 長さの基準
     * @param aoffset
     * @param b
     * @param boffset
     * @param ret
     * @return a OR b
     */
    public static byte[] or(byte[] a, int aoffset, byte[] b, int boffset, byte[] ret) {
        int min = Matics.min( ret.length, a.length - aoffset, b.length - boffset );
        for ( int i = 0; i < min; i++ ) {
            ret[i] = (byte)(a[aoffset + i] | b[boffset + i]);
        }
        if ( min < ret.length ) {
            if ( min < a.length - aoffset ) {
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
     * @deprecated まだ未確定
     * @param a
     * @param aoffset
     * @param b
     * @param boffset
     * @param ret
     * @param roffset
     * @return a XOR b
     */
    public static byte[] xor(byte[] a, int aoffset, byte[] b, int boffset, byte[] ret, int roffset) {
        int min = Matics.min( ret.length - roffset, a.length - aoffset, b.length - boffset );
        for ( int i = 0; i < min; i++ ) {
            ret[roffset + i] = (byte)(a[aoffset + i] ^ b[boffset + i]);
        }
        if ( min < ret.length - roffset ) {
            if ( min < a.length - aoffset ) {
                System.arraycopy(a, aoffset + min, ret, roffset + min, Math.min(ret.length, a.length - aoffset) - min);
            } else {
                System.arraycopy(b, boffset + min, ret, roffset + min, Math.min(ret.length, b.length - boffset) - min);
            }
        }
        return ret;
    }

    /**
     * XORを計算するよ
     * @param a バイト列 長さの基準
     * @param b バイト列
     * @return a ^ b
     */
    public static byte[] xor(byte[] a, byte[] b) {
        return xor(a, 0, b, 0, new byte[a.length], 0);
    }

    /**
     * a と b の xor 計算を a に戻す
     * @param a 結果もこっちへ
     * @param b
     * @return 
     */
    public static byte[] xorl(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        for ( int i = 0; i < len; i++ ) {
            a[i] ^= b[i];
        }
        return a;
    }

    public static long[] xorl(long[] a, long[] b) {
        int len = Math.min(a.length, b.length);
        for ( int i = 0; i < len; i++ ) {
            a[i] ^= b[i];
        }
        return a;
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
        int min = Matics.min( ret.length, a.length - aoffset, b.length - boffset );
        for ( int i = 0; i < min; i++ ) {
            ret[i] = a[aoffset + i] ^ b[boffset + i];
        }
        if ( min < ret.length ) {
            if ( min < a.length - aoffset ) {
                System.arraycopy(a, aoffset + min, ret, min, Math.min(ret.length, a.length - aoffset) - min);
            } else {
                System.arraycopy(b, boffset + min, ret, min, Math.min(ret.length, b.length - boffset) - min);
            }
        }
        return ret;
    }

    /**
     * aとbのxorを返す.
     * @param a 入力a
     * @param b 入力b
     * @return xor結果, 長さはaと同じ
     */
    public static long[] xor(long[] a, long[] b) {
        return xor(a, 0, b, 0, new long[a.length]);
    }

    public static byte[] not(byte[] a, int aoffset, byte[] ret) {
        int min = Matics.min( ret.length, a.length - aoffset );
        for ( int i = 0; i < min; i++ ) {
            ret[i] = (byte)(~a[aoffset + i]);
        }
        if ( min < ret.length ) {
            Arrays.fill(ret, min, ret.length, (byte)0xff);
        }
        return ret;
    }

    public static byte[] not(byte[] a) {
        return not(a, 0, new byte[a.length]);
    }

    /**
     * Big endian 左シフト
     * @param a 元列
     * @return 1ビット左シフト
     */
    public static byte[] shl(byte[] a) {
        byte[] n = new byte[a.length];
        int v = Byte.toUnsignedInt(a[0]);
        for (int i = 1; i < a.length; i++) {
            v = (v << 8) | Byte.toUnsignedInt(a[i]);
            n[i - 1] = (byte)(v >>> 7);
        }
        n[a.length - 1] = (byte)(v << 1);
        return n;
    }

    /**
     * Big endian 左シフト
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
     * @param a 配列
     * @param shift シフトビット数 とりあえず 0 から 7
     * @return シフトされた列
     */
    public static byte[] left(byte[] a, int shift) {
        byte[] b = new byte[a.length];
        int v = Byte.toUnsignedInt(a[0]);
        for (int i = 1; i < a.length ; i++) {
            v = (v << 8) | Byte.toUnsignedInt(a[i]);
            b[i - 1] |= (byte)(v >>> (8 - shift));
        }
        b[a.length - 1] = (byte)(v << shift);
        return b;
    }

    /**
     * 右1bitシフト
     * @param a 元列
     * @return 右シフト
     */
    public static byte[] shr(byte[] a) {
        byte[] n = new byte[a.length];
        for (int i = a.length - 1; i > 0; i--) {
            n[i] = (byte)((Byte.toUnsignedInt(a[i]) >> 1) | (Byte.toUnsignedInt(a[i - 1]) << 7));
        }
        n[0] = (byte)(Byte.toUnsignedInt(a[0]) >> 1);
        return n;
    }    

    /**
     * 右1bitシフト
     * @param a 非破壊
     * @return 右シフト
     */
    public static int[] shr(int[] a) {
        int[] n = new int[a.length];
        for (int i = a.length - 1; i > 0; i--) {
            n[i] = (a[i] >>> 1) | ( a[i - 1] << 31);
        }
        n[0] = a[0] >>> 1;
        return n;
    }    

    /**
     * 右1bitシフト
     * @param a 非破壊
     * @return 右シフト
     */
    public static long[] shr(long[] a) {
        long[] n = new long[a.length];
        for (int i = a.length - 1; i > 0; i--) {
            n[i] = (a[i] >>> 1) | ( a[i - 1] << 63);
        }
        n[0] = a[0] >>> 1;
        return n;
    }    

    /**
     * 右1bit rotate
     * @param a 非破壊
     * @return 
     */
    public static byte[] ror(byte[] a) {
        byte b = (byte)(a[a.length - 1] << 7);
        byte[] n = shr(a);
        n[0] |= b;
        return n;
    }    

    /**
     * 右1bit rotate
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
            n[i + 1] = (byte)(v >>> shift);
        }
        n[0] = (byte)(v >>> (shift + 8));
        return n;
    }    
}
