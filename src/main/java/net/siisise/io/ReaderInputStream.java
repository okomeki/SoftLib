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
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.siisise.lang.CodePoint;

/**
 * バイト列が必要な処理系にReader系を繋ぐ
 * InputStreamReaderの逆
 * とりあえずUTF-8出力前提
 * 100文字程度読んでおく
 */
public class ReaderInputStream extends InputStream {
    
    final PacketA pac = new PacketA();
    char[] pair;
    final Reader rd;
    final int bufferSize;
    boolean eof = false;
    
    ReaderInputStream(Reader r) {
        rd = r;
        bufferSize = 100;
    }
    
    ReaderInputStream(Reader r, int size) {
        rd = r;
        bufferSize = size;
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
        while ( !eof && pac.size() < bufferSize ) {
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
