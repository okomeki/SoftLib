package net.siisise.io;

/**
 * BASE64エンコーダ/デコーダ
 * RFC 2045
 * RFC 3548等もある
 * net.siisise.lang.String へ持っていっても可
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
 * @author okome 佐藤 雅俊 <okome@siisise.net>
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
    static char[] enc64 = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'};
*/
    /**
     * BASE64 用変換表
     */
    static final char[] enc64 = new char[64];
    static final char[] encurl = new char[64];
    
    /**
     * crypt / password 用変換表 (予定)
     */
    static final char[] encpass = new char[64];
    
    char[] encsrc;
    
//    static int[] dec;
    
    static {
        for (int i = 0; i <= 'z' - 'a'; i++) {
            enc64[i     ] = encurl[i     ] = encpass[i + 12] = (char)('A' + i);
            enc64[i + 26] = encurl[i + 26] = encpass[i + 38] = (char)('a' + i);
        }
        for (int i = 0; i < 10; i++) {
            enc64[i + 52] = encurl[i + 52] = encpass[i + 2] = (char)('0' + i);
        }
        enc64[62] = '+';
        enc64[63] = '/';
        encurl[62] = '-';
        encurl[63] = '_';
        encpass[0] = '.';
        encpass[1] = '/';
    }
    

    /**
     * 改行なしでエンコードされる
     */
    public BASE64() {
        setCols(0);
        encsrc = enc64;
    }
    
    /**
     * 改行位置指定あり。
     * 0以下だと改行なし
     */
    public BASE64(int size) {
        setCols(size);
        encsrc = enc64;
    }
    
    public interface Decoder {
        byte[] decode(String src);
    }
    
    class BaseDecoder implements Decoder {

        @Override
        public byte[] decode(String src) {
            return BASE64.this.decode(src);
        }
        
    }

    class PassDecoder implements Decoder {

        @Override
        public byte[] decode(String src) {
            return BASE64.this.decodePass(src);
        }
        
    }
    
    class URLDecoder implements Decoder {
        @Override
        public byte[] decode(String src) {
            return BASE64.this.decodeURL(src);
        }
        
    }
    
    /**
     * Type別にclass分けるまでの暫定かもしれない
     */
    public static enum Type {
        BASE64,
        PASSWORD,
        URL
    }

    // intだったころと互換?
    public static final Type BASE64 = Type.BASE64;
    public static final Type PASSWORD = Type.PASSWORD;
    public static final Type URL = Type.URL;

    static BASE64 selectType(Type t) {
        BASE64 b64 = new BASE64();
        b64.setType(t);
        return b64;
    }

    Decoder dec;
    
    /**
     * 種類の指定
     * 初期値はBASE64
     */
    void setType(Type type) {
        switch (type) {
            case PASSWORD:
                encsrc = encpass;
                dec = new PassDecoder();
                break;
            case URL:
                encsrc = encurl;
                dec = new URLDecoder();
                break;
            case BASE64:
            default:
                encsrc = enc64;
                dec = new BaseDecoder();
                break;
        }
    }
    
    /**
     * 6ビットを文字に変換
     * 変換テーブルとか必要?
     */
/*    private final char bout(int och) {
        return enc64[och & 0x3f];
/*        
        int out;
        och &= 0x3f;
        if (och <= 25) {
            out = 'A' + och;
        } else if (och <= 51) {
            out = 'a' + och - 26;
        } else if (och <= 61) {
            out = '0' + och - 52;
        } else {
            out = (och == 62) ? '+' : '/';
        }
        return (char)out;
    }
 */
    
    /**
     * 改行字数の指定。
     * 4の倍数以外でも動作するが推奨はしない
     * 0以下を指定すると改行しない
     *
     * MIME BASE64標準は76文字
     * SSL等は64文字
     */
    public void setCols(int col) {
        cols = col;
    }
    
    public int getCols() {
        return cols;
    }
    
    public final String encode( byte[] data ) {
        return encode( data, 0, data.length );
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
    public final String encode( byte[] data, int offset, int length ) {
        return String.valueOf( encodeToChar( data, offset, length ) );
    }
    
    /**
     * 文字配列型にエンコード
     * 3バイトを4文字に
     * サイズはあらかじめ計算可能なのでなんとかしておくと高速。
     * 
     * 改行サイズが指定されている場合、改行は最終行にも付ける
     * 
     * 2008.01.10 1.1版 offset, length対応、高速化
     * 2006.03.08 1.0版
     * JDK 5までは com.sun.にも同じ機能あり
     *  javax.mail にもあり
     *  それぞれ微妙に異なる
     * @param data
     * @param offset 開始位置
     * @param length ながさ
     * @return 文字として
     */
    public char[] encodeToChar(byte[] data, int offset, int length ) {
        int tmpData = 0, bit = 0;
        int col = 0;
        char[] b64;
        int b64offset = 0;
        {
            int b64size = (length+2) / 3 * 4; // 改行含まず
            if ( cols > 0 ) {
                b64size += ( b64size + cols - 1 ) / cols * 2; // 字数は4の倍数のみ想定
            }
            b64 = new char[b64size];
        }
        
        for (int idx = offset; idx < offset + length; idx++) {
            tmpData <<= 8;
            tmpData |= data[idx] & 0xff;
            bit += 8;
            do {
                bit -= 6;
                b64[b64offset++] = encsrc[(tmpData >> bit) & 0x3f];
                col++;
                if (col >= cols && cols > 0) { // 4文字単位で改行するのなら while の外でもいいかも
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
    
    public byte[] decode(String data) {
        return dec.decode(data);
    }

    /**
     * 4文字 を 3パイトへ
     * ヘッダフッタは仕様によって異なるので処理できません
     * まだ3バイト単位でないと処理できないかも
     * @param data 余計な文字のないBASE64コード
     * @return バイト列
     */
    public static byte[] decodeBase(String data) {
        PacketA pac = new PacketA();
        byte[] dt = new byte[3];
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
            if (c >= 'A' && c <='Z') {
                b |= c - 'A';
            } else if (c>= 'a' && c<='z') {
                b |= c - 'a' + 0x1a;
            } else if (c>='0' && c<='9') {
                b |= c - '0' + 0x34;
            } else if (c == '+') {
                b |= 0x3e;
            } else if (c == '/') {
                b |= 0x3f;
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
                dt[0] = (byte)((b >> 16) & 0xff);
                dt[1] = (byte)((b >> 8) & 0xff);
                dt[2] = (byte)(b & 0xff);
                pac.write(dt);
                len += 3;
                b = 0;
            }
        }
        dt = new byte[len];
        pac.read(dt);
        return dt;
    }

    /**
     * 4文字 を 3パイトへ
     * ヘッダフッタは仕様によって異なるので処理できません
     * まだ3バイト単位でないと処理できないかも
     * @param data 余計な文字のないBASE64コード
     */
    public static byte[] decodeURL(String data) {
        PacketA pac = new PacketA();
        byte[] dt = new byte[3];
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
            if (c >= 'A' && c <='Z') {
                b |= c - 'A';
            } else if (c>= 'a' && c<='z') {
                b |= c - 'a' + 0x1a;
            } else if (c>='0' && c<='9') {
                b |= c - '0' + 0x34;
            } else if (c == '-') {
                b |= 0x3e;
            } else if (c == '_') {
                b |= 0x3f;
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
                dt[0] = (byte)((b >> 16) & 0xff);
                dt[1] = (byte)((b >> 8) & 0xff);
                dt[2] = (byte)(b & 0xff);
                pac.write(dt);
                len += 3;
                b = 0;
            }
        }
        dt = new byte[len];
        pac.read(dt);
        return dt;
    }

    /**
     * 4文字 を 3パイトへ
     * ヘッダフッタは仕様によって異なるので処理できません
     * 改行コードなどは含めません
     * まだ3バイト単位でないと処理できないかも
     * @param data 余計な文字のないBASE64コード
     */
    public static byte[] decodePass(String data) {
        Packet pac = new PacketA();
        byte[] dt = new byte[3];
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
            if (c >= 'A' && c <='Z') {
                b |= c - 'A' + 26;
            } else if (c>= 'a' && c<='z') {
                b |= c - 'a' + 38;
            } else if (c>='0' && c<='9') {
                b |= c - '0' + 2;
            } else if (c == '.') {
                b |= 0x0;
            } else if (c == '/') {
                b |= 0x1;
            } else { // その他は無視する
                // 基本的にはここを通ることはない
                //  System.err.println("BASE64 password 対象外文字混入 異常処理");
                o--;
                b >>= 6;
                skip = true;
            }
            if (o % 4 == 3 && !skip) {
                dt[0] = (byte)((b >> 16) & 0xff);
                dt[1] = (byte)((b >> 8) & 0xff);
                dt[2] = (byte)(b & 0xff);
                pac.write(dt);
                len += 3;
                b = 0;
            }
        }
        dt = new byte[len];
        pac.read(dt);
        return dt;
    }
}
