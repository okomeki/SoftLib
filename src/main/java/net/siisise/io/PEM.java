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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * PEM系をBASE64から分離しておく
 */
public class PEM {

    /**
     * RFCエンコード.
     * pemかなにかのテキスト形式
     * 電子署名系で使用するヘッダフッタを付けます。
     * 64桁を指定しよう
     *
     * @param data バイト列ソース
     * @param type エンコードの名
     * @param fout テキスト出力先
     * @throws java.io.IOException
     */
    public void encode(byte[] data, String type, Writer fout) throws IOException {
        PrintWriter out = new PrintWriter(
                new BufferedWriter(fout));
        BASE64 b64 = new BASE64(64);
        out.print("-----BEGIN " + type + "-----\r\n");
        out.print(b64.encode(data));
        out.print("-----END " + type + "-----\r\n");
//        out.flush();
    }

    /**
     * ファイルに書き出します
     *
     * @param data データ
     * @param type エンコードの名
     * @param fileName 出力先ファイル名
     * @throws java.io.IOException
     */
    public void save(byte[] data, String type, String fileName) throws IOException {
        Writer out = new OutputStreamWriter(
                new FileOutputStream(fileName), "ASCII");
        try {
            encode(data, type, out);
            out.flush();
        } finally {
            out.close();
        }
    }

    /**
     * PEMをReaderから1つだけ読み込んだり.
     * typeは1種類のみ指定可能
     * RFC 7468 にまとまっている 
     *
     * @param type エンコードの名
     * @param fin テキストの入力
     * @return
     * @throws java.io.IOException
     */
    public static Map<String,Object> decode(String type, java.io.Reader fin) throws IOException {
        BufferedReader in = new BufferedReader(fin);
        String line;
        String begin = "-----BEGIN " + type + "-----";
        String end = "-----END " + type + "-----";
        StringBuilder src = new StringBuilder();

        do { // 頭確認
            line = in.readLine();
        } while (line != null && !line.equals(begin));
        
        Map<String,Object> m = new HashMap<>();

        if (line != null) {
            line = in.readLine();
            // 暗号化等のオプションには対応していないので読みとばす
            while (line.contains(": ")) {
                String[] hv = line.split(":",2);
                m.put(hv[0], hv[1].substring(1));

                line = in.readLine();
            }
            // 本文
            while (!line.equals(end)) {
                src.append(line);
                line = in.readLine();
            }
            m.put(null, BASE64.decodeBase(src.toString()));
        }
        return m;
    }

    /**
     * ファイルから読み込み
     *
     * @param type エンコードの名 BEGIN XXXXXXというところ
     * @param fileName ファイル名
     * @return
     * @throws java.io.IOException
     */
    public static Map<String,Object> load(String type, String fileName) throws IOException {
        InputStreamReader in = new InputStreamReader(new FileInputStream(fileName), "ASCII");
        Map<String,Object> m = decode(type, in);
        in.close();
        return m;
    }
    
}
