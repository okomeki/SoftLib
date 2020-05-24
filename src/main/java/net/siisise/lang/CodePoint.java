package net.siisise.lang;

import net.siisise.io.FrontPacket;

/**
 * RFC 3629
 * 拡張漢字、補助文字、補助漢字?
 * String のcodePoint(UCS-4)系をchar(UCS-2)風にする
 */
public class CodePoint {
    private final int[] chars;
    private final java.lang.String org;

    public CodePoint() {
        chars = new int[0];
        org = "";
    }

    public CodePoint( java.lang.String src ) {
        org = src;
        int size = src.codePointCount( 0, src.length() );
        chars = new int[size];
        for ( int index = 0; index < size; index++ ) {
            chars[index] = src.codePointAt( index );
        }
    }

    public int length() {
        return chars.length;
    }
    
    /**
     * 位置の文字を返す
     * @param index 位置
     * @return 文字
     */
    public int charAt(int index) {
        return org.codePointAt( index );
    }
    
    /**
     * 
     * @param cp 文字
     * @return 位置
     */
    public int indexOf(int cp) {
        return org.codePointAt( org.indexOf( cp ));
    }
    
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
            rd <<= 6;
            int c = pac.read();
            if ((c & 0xc0) != 0x80) {
                return -1;
            }
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
}
