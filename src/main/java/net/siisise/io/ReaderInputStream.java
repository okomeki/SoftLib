package net.siisise.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.siisise.lang.CodePoint;

/**
 * InputStreamReaderの逆
 * とりあえずUTF-8出力前提
 * 1000文字程度読んでおく
 */
public class ReaderInputStream extends InputStream {
    
    final PacketA pac = new PacketA();
    char[] pair;
    final Reader rd;
    boolean eof = false;
    
    ReaderInputStream(Reader r) {
        rd = r;
    }
    
    private void buffering() throws IOException {
        if ( eof ) return;
        int ch = rd.read();
        if ( ch < 0 ) {
            eof = true;
            return;
        }
        if ( ch >= 0xd800 && ch <= 0xdbff ) {
            if ( pair != null ) {
                ch = pair[0];
                byte[] bytes = CodePoint.utf8(ch);
                pac.write(bytes);
            }
            pair = new char[] {(char)ch,0};
            buffering();
            return;
        } else if ( ch >= 0xdc00 && ch <= 0xdfff ) {
            byte[] bytes;
            if ( pair != null ) {
                pair[1] = (char)ch;
                bytes = String.valueOf(pair).getBytes(StandardCharsets.UTF_8);
            } else {
                bytes = CodePoint.utf8(ch);
            }
            pac.write(bytes);
        } else {
            pair = null;
            byte[] bytes = CodePoint.utf8(ch);
            pac.write(bytes);
        }
    }

    @Override
    public int read() throws IOException {
        while ( !eof && pac.size() < 1024 ) {
            buffering();
        }
        return pac.read();
    }
    
    @Override
    public void close() throws IOException {
        rd.close();
    }
    
    /**
     * 正確な長さがわからない
     * @return
     * @throws IOException 
     */
    @Override
    public int available() throws IOException {
        return pac.size() + (rd.ready() ? 1 : 0);
    }
    
}
