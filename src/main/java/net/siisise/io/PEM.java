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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * PEM系をBASE64から分離しておく
 */
public class PEM implements TextEncode {
    
    public static final String RSA_PRIVATE_KEY = "RSA PRIVATE KEY";

    public static final String OPENSSH_PRIVATE_KEY = "OPENSSH PRIVATE KEY";

    private final String type;

    /**
     * 
     * @param type エンコードの名
     */
    public PEM(String type) {
        this.type = type;
    }

    /**
     * RFCエンコード.
     * pemかなにかのテキスト形式
     * 電子署名系で使用するヘッダフッタを付けます。
     * 64桁を指定しよう
     *
     * @param data バイト列ソース
     * @param fout テキスト出力先
     * @throws java.io.IOException
     */
    public void encode(byte[] data, Writer fout) throws IOException {
        fout.write(encode(data));
    }
    
    @Override
    public String encode(byte[] src, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        BASE64 b64 = new BASE64(64);
        sb.append("-----BEGIN ");
        sb.append(type);
        sb.append("-----\r\n");
        sb.append(b64.encode(src, offset, length));
        sb.append("-----END ");
        sb.append(type);
        sb.append("-----\r\n");
        return sb.toString();
    }

    /**
     * ファイルに書き出します
     *
     * @param data データ
     * @param fileName 出力先ファイル名
     * @throws java.io.IOException
     */
    public void save(byte[] data, String fileName) throws IOException {
        FileOutputStream out = new FileOutputStream(fileName);
        try {
            out.write(encode(data).getBytes(StandardCharsets.US_ASCII));
            out.flush();
        } finally {
            out.close();
        }
    }

    /**
     * 
     * @param base64
     * @return
     */
    @Override
    public byte[] decode(String base64) {
        Map<String, Object> map = decodeMap(base64);
        return (byte[]) map.get(null);
    }

    /**
     * ヘッダ、パラメータつきのデコード風
     * @param pem ヘッダつきPEM
     * @return 
     */
    @Override
    public Map<String,Object> decodeMap(String pem) {
        StringReader in = new StringReader(pem);
        try {
            return decodeMap(in);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * PEMをReaderから1つだけ読み込んだり.
     * typeは1種類のみ指定可能
     * RFC 7468 にまとまっている 
     *
     * @param pem テキストの入力
     * @return 本体デコードといろいろ.
     * @throws java.io.IOException
     */
    public Map<String,Object> decodeMap(java.io.Reader pem) throws IOException {
        BufferedReader in = new BufferedReader(pem);
        String line;
        String begin = "-----BEGIN " + type + "-----";
        String end = "-----END " + type + "-----";
        StringBuilder src = new StringBuilder();

        do { // 頭確認
            line = in.readLine();
        } while (line != null && !line.equals(begin));

        // てきとうなMIMEでこーど
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
     * @param fileName ファイル名
     * @return
     * @throws java.io.IOException
     */
    public Map<String,Object> load(String fileName) throws IOException {
        InputStreamReader in = new InputStreamReader(new FileInputStream(fileName), "ASCII");
        Map<String,Object> m = decodeMap(in);
        in.close();
        return m;
    }
    
}
