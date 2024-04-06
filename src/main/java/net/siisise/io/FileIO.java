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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 便利につかえる仮のクラス
 * URL系にまとめたい
 *
 */
public class FileIO {

    /**
     * DER? pkcs7 x509 など
     *
     * @param path File path
     * @return ファイルのバイト列
     * @throws java.io.IOException
     */
    public static byte[] binRead(String path) throws IOException {
        return binRead(new File(path));
    }
    
    public static byte[] binRead(File file) throws IOException {
        byte[] data;
        data = new byte[(int) file.length()];

        try (InputStream in = new FileInputStream(file)) {
            int o = in.read(data);
            in.close();
        }
        return data;
    }

    public static byte[] binRead(URL url) throws IOException {
        InputStream in = url.openStream();
        byte[] bin;
        try {
            bin = binRead(in);
        } finally {
            in.close();
        }
        return bin;
    }

    public static byte[] binRead(InputStream in) throws IOException {
        Packet pac = new PacketA();
        io(in,pac.getOutputStream());
        return pac.toByteArray();
    }

    /**
     * Socket等ブロックしない程度に読む.
     * @param in
     * @return
     * @throws IOException 
     */
    public static byte[] readAvailablie(InputStream in) throws IOException {
        Packet pac = new PacketA();
        available(in,pac.getOutputStream());
        return pac.toByteArray();
    }

    /**
     * inとoutを繋ぐだけ
     * @param in
     * @param out
     * @return
     * @throws IOException 
     */
    public static int io(InputStream in, OutputStream out) throws IOException {
        byte[] data = new byte[102400];
        int size = 0;
        int len;
        len = in.read(data);
        while (len >= 0) {
            out.write(data, 0, len);
            size += len;
            len = in.read(data);
        }
        out.flush();
        return size;
    }
    
    public static int available(InputStream in, OutputStream out) throws IOException {
        byte[] data = new byte[102400];
        int len = in.available();
        int flen = 0;
        while ( len > 0) {
            int s = in.read(data);
            if ( s < 0 ) break;
            out.write(data, 0, s);
            flen += s;
            len = in.available();
        }
        return flen;
   }
    
    /**
     * 
     * @param in
     * @param out
     * @return
     * @throws IOException 
     */
    public static int io(Reader in, Writer out) throws IOException {
        char[] data = new char[10000];
        int size = 0;
        int len;
        len = in.read(data);
        while (len >= 0) {
            out.write(data, 0, len);
            size += len;
            len = in.read(data);
        }
        out.flush();
        return size;
    }
    
    public static void copy(String src, String dst) throws IOException {
        File srcFile = new File(src);
        File dstFile = new File(dst);
        InputStream in = new BufferedInputStream(new FileInputStream(srcFile));
        FileOutputStream out = new FileOutputStream(dstFile);
        io(in,out);
        out.flush();
        in.close();
        out.close();
        dstFile.setLastModified(srcFile.lastModified());
    }

    public static void dump(byte[] src) {
        try {
            OutputStream o;
            o = new DumpOutputStream(new PrintWriter(System.out));
            o.write(src);
            o.flush();
            System.out.println();
        } catch (IOException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
