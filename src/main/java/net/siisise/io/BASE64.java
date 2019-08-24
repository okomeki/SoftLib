package net.siisise.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * BASE64エンコーダ/デコーダ
 * RFC 2045
 * RFC 3548等もある
 * net.siisise.lang.String へ持っていっても可
 * ApacheにBASE64もあるらしいがちょっと違う
 *
 * 改行コードはOSに依存せず通信の標準である\r\nに統一します。
 *
 * 2007/01/10 エンコードの高速化
 * 2006/11/09 GPLライセンス適用
 * 2006/10/25 0.3 RFCヘッダフッタ処理機能追加
 *
 * このコードのライセンスはGPLですが、他のソフトウェアに組み込みたい場合は、ご連絡ください。
 * 寄付歓迎
 *
 * @version 0.4
 * @author okome 佐藤 雅俊
 */
public class BASE64 {

    /**
     * エンコード時の1行の長さ
     * MIME 76
     * PKCS 64ぐらい
     */
    protected int cols;

    /**
     * テーブルにしてみるとこうなる。
     * コンストラクタで生成する方が省エネ
     */
    /*
    static char[] ENC64 = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'};
     */

    private char[] encsrc;
    private byte[] bytesrc;
    private int[] decsrc;

    private static final byte[] CRLF = {'\r', '\n'};

    /**
     * BASE64 用変換表
     */
    public static enum Type {
    /**
         * BASE64 用変換表
     */
        BASE64,
    /**
     * crypt / password 用変換表 (予定)
     */
        PASSWORD,
        /**
         * URL用修正付きBASE64
         */
        URL;
        char[] encsrc;
        byte[] bytesrc;
        int[] decsrc;
    }

    static {
        for (int i = 0; i <= 'z' - 'a'; i++) {
            Type.BASE64.encsrc[i    ] = Type.URL.encsrc[i    ] = Type.PASSWORD.encsrc[i +12] = (char)('A' + i);
            Type.BASE64.encsrc[i +26] = Type.URL.encsrc[i +26] = Type.PASSWORD.encsrc[i +38] = (char)('a' + i);
            Type.BASE64.bytesrc[i   ] = Type.URL.bytesrc[i   ] = Type.PASSWORD.bytesrc[i+12] = (byte)('A' + i);
            Type.BASE64.bytesrc[i+26] = Type.URL.bytesrc[i+26] = Type.PASSWORD.bytesrc[i+38] = (byte)('a' + i);
        }
        for (int i = 0; i < 10; i++) {
            Type.BASE64.encsrc[i + 52] = Type.URL.encsrc[i + 52] = Type.PASSWORD.encsrc[i + 2] = (char)('0' + i);
            Type.BASE64.bytesrc[i+ 52] = Type.URL.bytesrc[i+ 52] = Type.PASSWORD.bytesrc[i+ 2] = (byte)('0' + i);
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

        for (int i = 0; i < 128; i++) {
            Type.BASE64.decsrc[i] = Type.URL.decsrc[i] = Type.PASSWORD.decsrc[i] = -1;
        }
        for (int i = 0; i < 64; i++) {
            Type.BASE64.decsrc[Type.BASE64.encsrc[i]] = i;
            Type.URL.decsrc[Type.URL.encsrc[i]] = i;
            Type.PASSWORD.decsrc[Type.PASSWORD.encsrc[i]] = i;
        }
    }

    // intだったころと互換?
    public static final Type BASE64 = Type.BASE64;
    public static final Type PASSWORD = Type.PASSWORD;
    public static final Type URL = Type.URL;
    
    /**
     * 改行なしでエンコードされる
     */
    public BASE64() {
        this(0);
    }

    /**
     * 改行位置指定あり。
     * 0以下だと改行なし
     *
     * @param size
     */
    public BASE64(int size) {
        setCols(size);
        setType(Type.BASE64);
    }

    static BASE64 selectType(Type t) {
        BASE64 b64 = new BASE64();
        b64.setType(t);
        return b64;
    }

    /**
     * 種類の指定
     * 初期値はBASE64
     *
     * @param type BASE64の種類
     */
    public void setType(Type type) {
        switch (type) {
            case PASSWORD:
            case URL:
            case BASE64:
                break;
            default:
                type = Type.BASE64;
                break;
        }
        encsrc = type.encsrc;
        bytesrc = type.bytesrc;
        decsrc = type.decsrc;
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
        {
            int b64size = (length + 2) / 3 * 4; // 改行含まず
            if (cols > 0) {
                b64size += (b64size + cols - 1) / cols * 2; // 字数は4の倍数のみ想定
            }
            b64 = new char[b64size];
        }
        int last = offset + length;

        if (cols <= 0) { // 速い
            int l2 = last - 2;
            while (offset < l2) {
                int tmp = ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
                b64[b64offset++] = encsrc[tmp >>> 18];
                b64[b64offset++] = encsrc[(tmp >>> 12) & 0x3f];
                b64[b64offset++] = encsrc[(tmp >>> 6) & 0x3f];
                b64[b64offset++] = encsrc[tmp & 0x3f];
            }
        }

        for (int idx = offset; idx < last; idx++) {
            tmpData <<= 8;
            tmpData |= data[idx] & 0xff;
            bit += 8;
            do {
                bit -= 6;
                b64[b64offset++] = encsrc[(tmpData >> bit) & 0x3f];
                col++;
                if (cols > 0 && col >= cols) { // 4文字単位で改行するのなら while の外でもいいかも
                    b64[b64offset++] = '\r';
                    b64[b64offset++] = '\n';
                    col = 0;
                }
            } while (bit >= 6);
        }
        if (bit > 0) { // ビット残あり 4または 2ビット
            b64[b64offset++] = encsrc[(tmpData << (6 - bit)) & 0x3f];
            bit += (8 - 6);
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
//        Packet2 out;

        int b64size = (length + 2) / 3 * 4; // 改行含まず
        if (cols > 0) {
            b64size += (b64size + cols - 1) / cols * 2; // 字数は4の倍数のみ想定
        }

        out = new ArrayOutputStream(b64size);

        try {
            encodeToStream(data, out, offset, length);
        } catch (IOException ex) {
            // ないかも
        }
        return out.toByteArray();
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

        int b64size = (length + 2) / 3 * 4; // 改行含まず
        if (cols > 0) {
            b64size += (b64size + cols - 1) / cols * 2; // 字数は4の倍数のみ想定
        }

        int last = offset + length;
        if (cols <= 0) {
            int l2 = last - 2;
            byte[] n = new byte[4];
            while (offset < l2) {
                int tmp = ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
                n[0] = bytesrc[tmp >>> 18];
                n[1] = bytesrc[(tmp >>> 12) & 0x3f];
                n[2] = bytesrc[(tmp >>> 6) & 0x3f];
                n[3] = bytesrc[tmp & 0x3f];
                out.write(n);
                /*                out.write(bytesrc[tmp >>> 18]);
                out.write(bytesrc[(tmp >>> 12) & 0x3f]);
                out.write(bytesrc[(tmp >>> 6) & 0x3f]);
                out.write(bytesrc[tmp & 0x3f]);
                 */
            }
        }

        for (int idx = offset; idx < last; idx++) {
            tmpData <<= 8;
            tmpData |= data[idx] & 0xff;
            bit += 8;
            do {
                bit -= 6;
                out.write(bytesrc[(tmpData >> bit) & 0x3f]);
                col++;
                if (col >= cols && cols > 0) { // 4文字単位で改行するのなら while の外でもいいかも
                    out.write(CRLF);
                    col = 0;
                }
            } while (bit >= 6);
        }
        if (bit > 0) { // ビット残あり 4または 2ビット
            out.write(bytesrc[(tmpData << (6 - bit)) & 0x3f]);
            bit += (8 - 6);
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
        BASE64 b = new BASE64();
        b.setType(BASE64);
        return b.decode(data);
    }

    public static byte[] decodeURL(String data) {
        BASE64 b = new BASE64();
        b.setType(URL);
        return b.decode(data);
    }

    public static byte[] decodePass(String data) {
        BASE64 b = new BASE64();
        b.setType(PASSWORD);
        return b.decode(data);
    }

    /**
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
        int c;
        int len = 0;
        int b = 0;

        // 余計な文字(改行、スペース等)を取り除く
        // いまのところ不要
        boolean skip;
        // 抽出
        for (int i = 0, o = 0; i < data.length(); i++, o++) {
            c = data.charAt(i);
            b <<= 6;
            skip = false;
            if (c < 128 && decsrc[c] >= 0) {
                b |= decsrc[c];
            } else if (c == '=') {
                len--;
                // デコード終了でもよい
            } else { // その他は無視する
                // 基本的にはここを通ることはない
                // 改行などは別にした方がいいかも
                //  System.err.println("BASE64 対象外文字混入 異常処理");
                o--;
                b >>= 6;
                skip = true;
            }
            if (o % 4 == 3 && !skip) {
                tmp[0] = (byte) ((b >> 16) & 0xff);
                tmp[1] = (byte) ((b >> 8) & 0xff);
                tmp[2] = (byte) (b & 0xff);
                pac.write(tmp);
                len += 3;
                b = 0;
            }
        }
        tmp = new byte[len];
        pac.read(tmp);
        return tmp;
    }

    /**
     * RFCエンコード.
     * 電子署名系で使用するヘッダフッタを付けます。
     * 64桁を指定しよう
     *
     * @param data
     * @param type
     * @param fout
     * @throws java.io.IOException
     */
    public void encode(byte[] data, String type, Writer fout) throws IOException {
        PrintWriter out = new PrintWriter(
                new BufferedWriter(fout));

        out.print("-----BEGIN " + type + "-----\r\n");
        out.print(encode(data));
        out.print("-----END " + type + "-----\r\n");
//        out.flush();
    }

    /**
     * ファイルに書き出します
     *
     * @param data
     * @param type
     * @param fileName
     * @throws java.io.IOException
     */
    public void save(byte[] data, String type, String fileName) throws IOException {
        java.io.Writer out = new OutputStreamWriter(
                new FileOutputStream(fileName), "ASCII");
        encode(data, type, out);
        out.flush();
        out.close();
    }

    /**
     * Readerから1つだけ読み込んだり.
     * typeは1種類のみ指定可能
     *
     * @param type
     * @param fin
     * @return
     * @throws java.io.IOException
     */
    public static byte[] decode(String type, java.io.Reader fin) throws IOException {
        BufferedReader in = new BufferedReader(fin);
        String line;
        String begin = "-----BEGIN " + type + "-----";
        String end = "-----END " + type + "-----";
        byte[] data = null;
        StringBuilder src = new StringBuilder();

        do { // 頭確認
            line = in.readLine();
        } while (line != null && !line.equals(begin));

        if (line != null) {
            line = in.readLine();
            // 暗号化等のオプションには対応していないので読みとばす
            while (line.contains(": ")) {

                line = in.readLine();
            }
            // 本文
            while (!line.equals(end)) {
                src.append(line);
                line = in.readLine();
            }
            data = decodeBase(src.toString());
        }
        return data;
    }

    /**
     * ファイルから読み込み
     *
     * @param type BEGIN XXXXXXというところ
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public static byte[] load(String type, String fileName) throws IOException {
        InputStreamReader in = new InputStreamReader(new FileInputStream(fileName), "ASCII");
        byte[] data = decode(type, in);
        in.close();
        return data;
    }
}
