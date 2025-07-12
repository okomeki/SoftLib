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

import java.io.CharConversionException;
import java.io.FilterReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * BufferedReader が死ぬので
 * UTF-16固定
 */
public class NetReader extends FilterReader {

    boolean lastCR = false;
    private final static char CR = '\r';
    private final static char LF = '\n';

    /**
     * Readerつなぎ.
     * @param r 読み出し元
     */
    public NetReader(Reader r) {
        super(r);
    }

    /**
     * 不正な文字コードが混入していないかUnicodeに変換してから一応チェック
     *
     * @return 1行
     * @throws java.io.IOException
     */
    public String readLine() throws IOException {
        byte[] data = readByteLine();
        if ( data == null ) return null;
        String string = new String(data, "iso-10646-ucs-2");
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
