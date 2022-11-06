/*
 * Copyright 2019-2022 Siisise Net.
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

import java.util.Arrays;
import java.util.stream.IntStream;
import net.siisise.io.FrontPacket;
import net.siisise.block.ReadableBlock;

/**
 * RFC 3629
 * 拡張漢字、補助文字、補助漢字?
 * String のcodePoint(UCS-4)系をchar(UCS-2)風にする
 * 想定範囲は当面 UTF-32 (U+0-U+10FFFF) の範囲内
 * CharSequence は charAtの戻り値型がchar/intで異なるため継承できないのでcodePointAtにしようかな
 */
public class CodePoint {

    /**
     * CodePoint(UCS-4)化した文字列
     * UTF-32として扱っても問題ない範囲
     */
    private final int[] chars;
    /**
     * 元のUTF-16の文字列
     */
    private final java.lang.String org;

    public CodePoint() {
        chars = new int[0];
        org = "";
    }

    /**
     * 文字列をUCS-4で扱う方向に初期化
     *
     * @param src Java(UTF-16)の文字列
     */
    public CodePoint(java.lang.String src) {
        org = src;
        int size = src.codePointCount(0, src.length());
        chars = new int[size];
        for (int index = 0; index < size; index++) {
            chars[index] = src.codePointAt(index);
        }
    }

    /**
     * code point 長
     *
     * @return code point(UCS-4/UTF-32) 単位の文字数
     */
    public int length() {
        return chars.length;
    }

    /**
     * 位置の文字を返す
     * codePointAt で揃えた方がいい気がした
     *
     * @param index 位置
     * @return UCS-4/UTF-32文字
     */
    public int charAt(int index) {
        return chars[index];
//        return org.codePointAt( index ); // 同じ結果のはず
    }

    /**
     * 文字の位置を返すはずだったもの
     * cpがucs-2の0-0xffffときcharsにUCS-4が混ざっているとString#indexOfと結果が違う予定
     *
     * @param cp 文字
     * @return 位置
     */
    public int indexOf(int cp) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == cp) {
                return i;
            }
        }
        return -1;
    }

    /**
     * String互換 空文字列?
     *
     * @return
     */
    public boolean isEmpty() {
        return chars.length == 0;
    }

    /**
     * CodePoint-8をUCSに変換.
     * 不正組は-1
     *
     * @param pac
     * @return UCS-4または不正の場合-1
     */
    public static int utf8(FrontPacket pac) {
        int rd = pac.read();
        if (rd < 0) {
            return -1;
        }
        int len;
        int min;
        int srd = rd;
        if (rd < 0x80) {        // 0xxx xxxx 1バイト 7bit 00 - 7f
            return rd;
        } else if (rd < 0xc0) { // 10xx xxxx 80 - 7ff 2バイト目以降
            pac.backWrite(rd);
            return -1;
        } else if (rd < 0xe0) { // 110x xxxx 2バイト 11bit
            rd &= 0x1f;
            len = 1;
            min = 0x80;
        } else if (rd < 0xf0) { // 1110 xxxx 3バイト 16bit
            rd &= 0xf;
            len = 2;
            min = 0x800;
        } else {                  // 1111 0xxx 4バイト 21bit
            rd &= 0x7;
            len = 3;
            min = 0x10000;
        }
        
        byte[] d = new byte[len];
        int s = pac.read(d);
        if ( s < len ) {
            if ( s > 0 ) {
                pac.backWrite(d,0,s);
            }
            pac.backWrite(srd);
            return -1;
        }

        for (int i = 0; i < len; i++) {
            int c = d[i] & 0xff;

            if ((c & 0xc0) != 0x80) {
                pac.dbackWrite(d);
                pac.backWrite(srd);
                return -1;
            }
            rd <<= 6;
            rd |= (c & 0x3f);
        }
        if (rd < min || rd > 0x10ffff) { // ToDo: 要戻り
            pac.backWrite(d);
            pac.backWrite(srd);
            return -1;
        }
        return rd;
    }

    /**
     * CodePoint-8をUCSに変換.
     * 不正組は-1
     *
     * @param pac
     * @return UCS-4または不正の場合-1
     */
    public static int utf8(ReadableBlock pac) {
        int mark = pac.backSize();
        int rd = pac.read();
        int len;
        int min;
        if (rd < 0) {
            return -1;
        }
        if (rd < 0x80) {        // 0xxx xxxx 1バイト 7bit 00 - 7f
            return rd;
        } else if (rd < 0xc0) { // 10xx xxxx 80 - 7ff 2バイト目以降
            pac.seek(mark);
            return -1;
        } else if (rd < 0xe0) { // 110x xxxx 2バイト 11bit
            rd &= 0x1f;
            len = 1;
            min = 0x80;
        } else if (rd < 0xf0) { // 1110 xxxx 3バイト 16bit
            rd &= 0xf;
            len = 2;
            min = 0x800;
        } else {                  // 1111 0xxx 4バイト 21bit
            rd &= 0x7;
            len = 3;
            min = 0x10000;
        }

        byte[] d = new byte[len];
        int s = pac.read(d);
        if ( s < len ) {
            pac.seek(mark);
            return -1;
        }

        for (int i = 0; i < len; i++) {
            int c = d[i] & 0xff;

            if ((c & 0xc0) != 0x80) {
                pac.seek(mark);
                return -1;
            }
            rd <<= 6;
            rd |= (c & 0x3f);
        }
        if (rd < min || rd > 0x10ffff) { // ToDo: 戻らないのか?
            pac.seek(mark);
            return -1;
        }
        return rd;
    }

    /**
     * UCS to UTF-8
     *
     * @param ch
     * @return
     */
    public static byte[] utf8(int ch) {
        if (ch < 0x80) {
            return new byte[]{(byte) ch};
        } else if (ch < 0x800) {
            return new byte[]{
                (byte) (0xc0 | (ch >> 6)),
                (byte) (0x80 | (ch & 0x3f))
            };
        } else if (ch < 0x10000) {
            return new byte[]{
                (byte) (0xe0 | (ch >> 12)),
                (byte) (0x80 | ((ch >> 6) & 0x3f)),
                (byte) (0x80 | (ch & 0x3f))
            };
        } else if (ch < 0x110000) {
            return new byte[]{
                (byte) (0xf0 | (ch >> 18)),
                (byte) (0x80 | ((ch >> 12) & 0x3f)),
                (byte) (0x80 | ((ch >> 6) & 0x3f)),
                (byte) (0x80 | (ch & 0x3f))
            };
        }
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public java.lang.String toString() {
        return org;
    }

    /**
     * CharSequenceとして振る舞う?
     * codePointsと同じ結果を返す?
     *
     * @return
     */
    public IntStream chars() {
        return Arrays.stream(chars);
    }

    public IntStream codePoints() {
        return Arrays.stream(chars);
    }

}
