package net.siisise.lang;

import java.util.Arrays;
import java.util.stream.IntStream;
import net.siisise.io.FrontPacket;

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
     * @param src Java(UTF-16)の文字列
     */
    public CodePoint( java.lang.String src ) {
        org = src;
        int size = src.codePointCount( 0, src.length() );
        chars = new int[size];
        for ( int index = 0; index < size; index++ ) {
            chars[index] = src.codePointAt( index );
        }
    }

    /**
     * code point 長
     * @return code point(UCS-4/UTF-32) 単位の文字数
     */
    public int length() {
        return chars.length;
    }
    
    /**
     * 位置の文字を返す
     * codePointAt で揃えた方がいい気がした
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
        for ( int i = 0; i < chars.length; i++ ) {
            if ( chars[i] == cp ) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * String互換 空文字列?
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
        int len;
        int min;
        if (rd < 0) {
            return -1;
        }
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

        for (int i = 0; i < len; i++) {
            int c = pac.read();
            
            if ((c & 0xc0) != 0x80) {
                if ( c >= 0) {
                    pac.backWrite(c);
                }
                for ( int x = 0; x < i; x++ ) {
                    pac.backWrite((rd & 0x3f) | 0x80);
                    rd >>>= 6;
                }
                pac.backWrite(rd | (0xf0 & (0xf80 >> len)));
                return -1;
            }
            rd <<= 6;
            rd |= (c & 0x3f);
        }
        if (rd < min || rd > 0x10ffff) {
            return -1;
        }
        return rd;
    }

    /**
     * UCS to UTF-8
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
     * @return 
     */
    public IntStream chars() {
        return Arrays.stream(chars);
    }
    
    public IntStream codePoints() {
        return Arrays.stream(chars);
    }

}
