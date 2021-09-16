package net.siisise.io;

import java.io.CharConversionException;
import java.io.FilterReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * BufferedReader が死ぬので
 * UTF-16固定
 * @author okome
 */
public class NetReader extends FilterReader {

    boolean lastCR = false;
    private final static char CR = '\r';
    private final static char LF = '\n';
    Charset ucs2 = java.nio.charset.StandardCharsets.UTF_16;

    /**
     *
     * @param r
     */
    public NetReader(Reader r) {
        super(r);
    }

    /**
     * 不正な文字コードが混入していないかUnicodeに変換してから一応チェック
     *
     * @return
     * @throws java.io.IOException
     */
    public String readLine() throws IOException {
//        String encBak = this.encode;
//        setEncode( encode ); // 文字コードによっては改行コードが変わる可能性があるので入れる方がいい?
        byte[] data = readByteLine();
        if ( data == null ) return null;
        String string = new String(data, "iso-10646-ucs-2");
//        setEncode( encBak );
        if (string.indexOf('\r') >= 0 || string.indexOf('\n') >= 0) {
            // 偽UTF-8とかで改行コードが漏れた
            throw new CharConversionException();
        }
        return string;
    }

    public byte[] readByteLine() throws IOException {
        PacketA pac2 = new PacketA();
        Writer opw = new OutputStreamWriter(pac2.getOutputStream(),"iso-10646-ucs-2"); // ucs-2かutf-16?
        int ch;
        ch = super.read();
        if ( ch == -1) return null;
        do {
            if (ch == CR) { // 改行なのでLFがあろうがなかろうが
                lastCR = true;
                break;
            } else if (ch == LF) {
                if (!lastCR) { // LF単体
                    break;
                } else { // CRLFセット  前がCRなら改行済なので何もしない
                    lastCR = false;
                }
            } else {
                lastCR = false;
                //pac2.write(ch);
                opw.write(ch);
                opw.flush();
            }
            ch = super.read();
        } while ( ch != -1 );
        byte[] a = new byte[(int) pac2.length()];
        pac2.read(a);
        return a;
    }


}
