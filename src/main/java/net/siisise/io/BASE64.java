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
package net.siisise.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * BASE64エンコーダ/デコーダ。
 * RFC 2045
 * RFC 3548等もある
 * RFC 4648 Section 5 base64url
 * ApacheにBASE64もあるらしいがちょっと違う
 *
 * 改行コードはOSに依存せず通信の標準である\r\nに統一します。
 *
 * 2019/09/14 BASE64URLのデコード修正
 * 2019/08/28 複数バージョンを統合
 * 2007/01/10 エンコードの高速化
 * 2006/11/09 GPLライセンス適用
 * 2006/10/25 0.3 RFCヘッダフッタ処理機能追加
 *
 */
public class BASE64 {

    /**
     * エンコード時の1行の長さ
     * MIME 76
     * PKCS 64ぐらい
     */
    protected int cols;

    private static final byte[] CRLF = {'\r', '\n'};

    /**
     * BASE64 用変換表
     */
    public static enum Type {
        /** BASE64 用変換表 */
        BASE64,
        /** crypt / password 用変換表 (予定) */
        PASSWORD,
        /** URL用修正付きBASE64 */
        URL,
        /** 16進数を拡張したもの */
        HEX64;
        
        char[] encsrc = new char[64];
        byte[] bytesrc = new byte[64];
        int[] decsrc = new int[128];
    }

    private Type type;
    private boolean padding = true;

    static {
        for (int i = 0; i <= 'z' - 'a'; i++) {
            Type.BASE64.encsrc[i    ] = Type.URL.encsrc[i    ] = Type.PASSWORD.encsrc[i +12] = Type.HEX64.encsrc[i + 36] = (char)('A' + i);
            Type.BASE64.encsrc[i +26] = Type.URL.encsrc[i +26] = Type.PASSWORD.encsrc[i +38] = Type.HEX64.encsrc[i + 10] = (char)('a' + i);
            Type.BASE64.bytesrc[i   ] = Type.URL.bytesrc[i   ] = Type.PASSWORD.bytesrc[i+12] = Type.HEX64.bytesrc[i+ 36] = (byte)('A' + i);
            Type.BASE64.bytesrc[i+26] = Type.URL.bytesrc[i+26] = Type.PASSWORD.bytesrc[i+38] = Type.HEX64.bytesrc[i+ 10] = (byte)('a' + i);
        }
        for (int i = 0; i < 10; i++) {
            Type.BASE64.encsrc[i + 52] = Type.URL.encsrc[i + 52] = Type.PASSWORD.encsrc[i + 2] = Type.HEX64.encsrc[i]  = (char)('0' + i);
            Type.BASE64.bytesrc[i+ 52] = Type.URL.bytesrc[i+ 52] = Type.PASSWORD.bytesrc[i+ 2] = Type.HEX64.bytesrc[i] = (byte)('0' + i);
        }
        Type.BASE64.encsrc[62] = '+';
        Type.BASE64.encsrc[63] = '/';
        Type.BASE64.bytesrc[62] = '+';
        Type.BASE64.bytesrc[63] = '/';
        Type.PASSWORD.encsrc[0] = '.';
        Type.PASSWORD.encsrc[1] = '/';
        Type.PASSWORD.bytesrc[0] = '.';
        Type.PASSWORD.bytesrc[1] = '/';
        Type.URL.encsrc[62] = '-';
        Type.URL.encsrc[63] = '_';
        Type.URL.bytesrc[62] = '-';
        Type.URL.bytesrc[63] = '_';
        Type.HEX64.encsrc[62] = '-';
        Type.HEX64.encsrc[63] = '_';
        Type.HEX64.bytesrc[62] = '-';
        Type.HEX64.bytesrc[63] = '_';

        for (int i = 0; i < 128; i++) {
            Type.BASE64.decsrc[i] = Type.URL.decsrc[i] = Type.PASSWORD.decsrc[i] = Type.HEX64.decsrc[i] = -1;
        }
        for (int i = 0; i < 64; i++) {
            Type.BASE64.decsrc[Type.BASE64.encsrc[i]] = i;
            Type.URL.decsrc[Type.URL.encsrc[i]] = i;
            Type.PASSWORD.decsrc[Type.PASSWORD.encsrc[i]] = i;
            Type.HEX64.decsrc[Type.HEX64.encsrc[i]] = i;
        }
    }

    // intだったころと互換?
    public static final Type BASE64 = Type.BASE64;
    public static final Type PASSWORD = Type.PASSWORD;
    public static final Type URL = Type.URL;
    public static final Type HEX64 = Type.HEX64;

    /**
     * 簡易版BASE64処理装置コンストラクタ。
     * 改行なしでエンコードされる
     */
    public BASE64() {
        this(0);
    }

    /**
     * 改行位置指定ありBASE64処理装置コンストラクタ。
     * @param size 出力時の1行のサイズ 0は改行なし
     */
    public BASE64(int size) {
        this(Type.BASE64, true, size);
    }

    /**
     * 符号の種類と改行幅の指定できるBASE64処理装置コンストラクタ。
     * @param type 符号の種類 BASE64かPASSWORDかURL
     * @param size 出力時の1行のサイズ 0は改行なし
     */
    public BASE64(Type type, int size) {
        this(type, type != URL && type != HEX64, size);
    }
    
    /**
     * 
     * @param type BASE64,PASSWORD,URLなど指定可能
     * @param padding パディングをつけるか?
     * @param size 出力時の1行のサイズ 0は改行なし
     */
    public BASE64(Type type, boolean padding, int size) {
        setCols(size);
        setPadding(padding);
        setType(type);
    }

    static BASE64 selectType(Type t) {
        return new BASE64(t,0);
    }

    /**
     * 種類の指定。
     * 初期値はBASE64
     *
     * @param type BASE64の種類
     */
    public void setType(Type type) {
        switch (type) {
            case PASSWORD:
            case URL:
            case BASE64:
            case HEX64:
                break;
            default:
                type = Type.BASE64;
                break;
        }
        this.type = type;
    }

    public void setPadding(boolean pad) {
        padding = pad;
    }
    
    /**
     * 改行字数の指定。
     * 4の倍数以外でも動作するが推奨はしない
     * 0以下を指定すると改行しない
     *
     * MIME BASE64標準は76文字
     * SSL等は64文字
     *
     * @param col
     */
    public void setCols(int col) {
        cols = col;
    }

    public int getCols() {
        return cols;
    }

    public final String encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    /**
     * エンコード
     * 制限事項 String で格納できる長さより長くなるものには対応できない。
     *
     * @param data 元バイナリデータ
     * @param offset 開始位置
     * @param length サイズ
     * @return 文字列型
     */
    public final String encode(byte[] data, int offset, int length) {
        return String.valueOf(encodeToChar(data, offset, length));
    }

    /**
     * 文字配列型にエンコード.
     * 3バイトを4文字に
     * サイズはあらかじめ計算可能なのでなんとかしておくと高速。
     *
     * 改行サイズが指定されている場合、改行は最終行にも付ける
     *
     * 2008.01.10 1.1版 offset, length対応、高速化
     * 2006.03.08 1.0版
     * JDK 5までは com.sun. にも同じ機能あり.
     * javax.mail にもあり
     * それぞれ微妙に異なる
     *
     * @param data
     * @param offset 開始位置
     * @param length ながさ
     * @return 文字として
     */
    public char[] encodeToChar(byte[] data, int offset, int length) {
        int tmpData = 0, bit = 0;
        int col = 0;
        char[] b64;
        int b64offset = 0;

        int b64size = b64size(length);
        b64 = new char[b64size];

        int last = offset + length;

        if (cols <= 0) { // 速い
            int l2 = last - 2;
            while (offset < l2) {
                int tmp = ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
                b64[b64offset++] = type.encsrc[tmp >>> 18];
                b64[b64offset++] = type.encsrc[(tmp >>> 12) & 0x3f];
                b64[b64offset++] = type.encsrc[(tmp >>> 6) & 0x3f];
                b64[b64offset++] = type.encsrc[tmp & 0x3f];
            }
        }

        for (int idx = offset; idx < last; idx++) {
            tmpData <<= 8;
            tmpData |= data[idx] & 0xff;
            bit += 8;
            do {
                bit -= 6;
                b64[b64offset++] = type.encsrc[(tmpData >> bit) & 0x3f];
                col++;
                if (cols > 0 && col >= cols) { // 4文字単位で改行するのなら while の外でもいいかも
                    b64[b64offset++] = '\r';
                    b64[b64offset++] = '\n';
                    col = 0;
                }
            } while (bit >= 6);
        }
        if (bit > 0) { // ビット残あり 4または 2ビット
            b64[b64offset++] = type.encsrc[(tmpData << (6 - bit)) & 0x3f];
            bit += (8 - 6);
            if ( padding ) {
                do { // BASE64URLでは不要かもしれない
                    // 2 -> 10 -> 4 ->
                    b64[b64offset++] = '=';
                    bit -= 6;
                    if (bit < 0) {
                        bit += 8;
                    }
                    /*
                    col++;
                    // ここにも改行処理は必要?

                    if (col >= max) {
                        b64[b64offset++] = '\r';
                        b64[b64offset++] = '\n';
                        col = 0;
                    }
                     */
                } while (bit > 0); // ビットあまりの場合なので比較はあとでいい
            }
        }
        if (cols > 0 && col > 0) {
            b64[b64offset++] = '\r';
            b64[b64offset++] = '\n';
        }

        return b64;
    }

    /**
     * ASCII文字のバイト型に変換する。
     * ISO-8859-1なのでデータ節約用
     * Streamを使ってみた。使わないときはToCharと同じにできる
     *
     * @param data
     * @param offset
     * @param length
     * @return
     */
    public byte[] encodeToByte(byte[] data, int offset, int length) {
        ArrayOutputStream out;

        int b64size = b64size(length);
        out = new ArrayOutputStream(b64size);

        try {
            encodeToStream(data, out, offset, length);
        } catch (IOException ex) {
            // ないかも
        }
        return out.toByteArray();
    }
    
    /**
     * バイト列のBASE64符号化時のサイズを計算するだけ。
     * @param length 変換元バイト列の長さ
     * @return BASE64符号化時のサイズ
     */
    private int b64size(int length) {
        int b64size = (length + 2) / 3 * 4; // 改行含まず
        if (cols > 0) {
            b64size += (b64size + cols - 1) / cols * 2; // 字数は4の倍数のみ想定
        }
        if ( !padding && length %3 > 0 ) { // パディングなし
            b64size += length % 3 - 3;
        }
        return b64size;
    }

    /**
     * ASCII文字BASE64のStream出力。
     * ISO-8859-1なのでデータ節約用
     * 速くない
     *
     * @param data
     * @param out
     * @param offset
     * @param length
     * @return BASE64の長さ
     * @throws java.io.IOException
     */
    public int encodeToStream(byte[] data, OutputStream out, int offset, int length) throws IOException {
        int tmpData = 0, bit = 0;
        int col = 0;

        int b64size = b64size(length);
        int last = offset + length;
        if (cols <= 0) {
            int l2 = last - 2;
            byte[] n = new byte[4];
            while (offset < l2) {
                int tmp = ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
                n[0] = type.bytesrc[tmp >>> 18];
                n[1] = type.bytesrc[(tmp >>> 12) & 0x3f];
                n[2] = type.bytesrc[(tmp >>> 6) & 0x3f];
                n[3] = type.bytesrc[tmp & 0x3f];
                out.write(n);
            }
        }

        for (int idx = offset; idx < last; idx++) {
            tmpData <<= 8;
            tmpData |= data[idx] & 0xff;
            bit += 8;
            do {
                bit -= 6;
                out.write(type.bytesrc[(tmpData >> bit) & 0x3f]);
                col++;
                if (col >= cols && cols > 0) { // 4文字単位で改行するのなら while の外でもいいかも
                    out.write(CRLF);
                    col = 0;
                }
            } while (bit >= 6);
        }
        if (bit > 0) { // ビット残あり 4または 2ビット
            out.write(type.bytesrc[(tmpData << (6 - bit)) & 0x3f]);
            bit += (8 - 6);
            if ( padding ) {
                do {
                    // 2 -> 10 -> 4 ->
                    out.write('=');
                    bit -= 6;
                    if (bit < 0) {
                        bit += 8;
                    }
                    /*
                    col++;
                    // ここにも改行処理は必要?

                    if (col >= max) {
                        b64[b64offset++] = '\r';
                        b64[b64offset++] = '\n';
                        col = 0;
                    }
                     */
                } while (bit > 0); // ビットあまりの場合なので比較はあとでいい
            }
        }
        if (cols > 0 && col > 0) {
            out.write(CRLF);
        }

        return b64size;
    }

    /**
     * 4文字 を 3パイトへ.
     * ヘッダフッタは仕様によって異なるので処理できません.
     * まだ3バイト単位でないと処理できないかも
     *
     * @param data 余計な文字のないBASE64コード
     * @return
     */
    public static byte[] decodeBase(String data) {
        BASE64 b = new BASE64(BASE64,0);
        return b.decode(data);
    }

    /**
     * URLエンコードのBASE64デコード
     * @param data URL符号化データ
     * @return 復元済みデータ
     */
    public static byte[] decodeURL(String data) {
        BASE64 b = new BASE64(URL,0);
        return b.decode(data);
    }

    /**
     * パスワードエンコードのデコード
     * 
     * @param data PASSWORD符号化データ
     * @return 復元済みデータ
     */
    public static byte[] decodePass(String data) {
        BASE64 b = new BASE64(PASSWORD,0);
        return b.decode(data);
    }

    /**
     * 独自HEX64エンコードのデコード
     * 
     * @param data HEX64符号化データ
     * @return 復元済みデータ
     */
    public static byte[] decodeHex64(String data) {
        BASE64 b = new BASE64(HEX64,0);
        return b.decode(data);
    }

    /**
     * BASE64文字列をデータに復元する。
     * 4文字 を 3パイトへ.
     * ヘッダフッタは仕様によって異なるので処理できません.
     * まだ3バイト単位でないと処理できないかも.
     *
     * @param data 余計な文字のないBASE64コード
     * @return バイト列
     */
    public byte[] decode(String data) {
        PacketA pac = new PacketA();
        byte[] tmp = new byte[3];
        int ch;
        int len = 0;
        int tmpbits = 0;

        // 余計な文字(改行、スペース等)を取り除く
        // いまのところ不要
        boolean skip;
        // 抽出
        int o = 0;
        for (int i = 0; i < data.length(); i++, o++) {
            ch = data.charAt(i);
            tmpbits <<= 6;
            skip = false;
            if (ch < 128 && type.decsrc[ch] >= 0) {
                tmpbits |= type.decsrc[ch];
            } else if (ch == '=') {
                len--; // 最後に捨てる文字数
                // 4文字そろってからデコード終了する
            } else { // その他は無視する
                // 基本的にはここを通ることはない
                // 改行などは別にした方がいいかも
                //  System.err.println("BASE64 対象外文字混入 異常処理");
                o--;
                tmpbits >>= 6;
                skip = true;
            }
            if (o % 4 == 3 && !skip) { // パディング込みで4文字必要
                tmp[0] = (byte) ((tmpbits >> 16) & 0xff);
                tmp[1] = (byte) ((tmpbits >> 8) & 0xff);
                tmp[2] = (byte) (tmpbits & 0xff);
                pac.write(tmp);
                len += 3;
                tmpbits = 0;
            }
        }
        if ( !padding ) {
            o  = o % 4; // 0123
            if ( o >= 2 ) {
                tmpbits <<= 6*(4-o);
                tmp[0] = (byte) ((tmpbits >> 16) & 0xff);
                tmp[1] = (byte) ((tmpbits >> 8) & 0xff);
                pac.write(tmp);
                len += o-1;
            }
        }
        
        tmp = new byte[len];
        pac.read(tmp);
        return tmp;
    }
}
