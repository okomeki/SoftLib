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

/**
 * Hexというよりバイト列変換の主なもの.
 * Number べーす
 */
public class Hex {

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
        return toByteArray(src.toString());
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
     * @param txt 2の倍数の長さ限定 16進数文字列
     * @param offset txt の16進数の位置
     * @param length txt の16進数の長さ
     * @return バイト列
     */
    public static byte[] toByteArray(char[] txt, int offset, int length) {
        int bit = 4;
        byte[] data = new byte[(length * bit + 7) / 8];
        int bd = 0;
        int bitlen = 0;
        int j = 0;
        for (int i = 0; i < length; i++) {
            int a = txt[offset + i];
            if (a >= '0' && a <= '9') {
                a -= '0';
            } else if (a >= 'a' && a <= 'z') {
                a -= 'a' - 10;
            } else if (a >= 'A' && a <= 'Z') {
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
     * 2進数からBASE64まで対応したりしなかったり
     * BigIntegerでまわすのでスピードは期待しない方向で
     * 
     * 36進数までは 0-9,a-z,A-Z
     * 62進数まではa-zとA-Zを区別する
     * 64進数までは記号をいくつか
     * 
     * @param txt baae64まで
     * @param radix 64まで
     * @return 
     */
    public static byte[] toByteArray(char[] txt, int radix) {
        int len = txt.length;
        BigInteger num = BigInteger.ZERO;
        BigInteger rad = BigInteger.valueOf(radix);

        for (int i = 0; i < len; i++) {
            int a = txt[i];
            if (a >= '0' && a <= '9') {
                a -= '0';
            } else if (a >= 'a' && a <= 'z') { // 10 - 35
                a -= 'a' - 10;
            } else if (a >= 'A' && a <= 'Z') { // 36 - 61
                if ( radix > 36) { // てきとー
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
            num = num.multiply(rad);
            BigInteger n = BigInteger.valueOf(a);
            num = num.add(n);
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
    
}
