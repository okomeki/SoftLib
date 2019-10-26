package net.siisise.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 便利につかえる仮のクラス
 * URL系にまとめたい
 *
 * @author okome
 */
public class FileIO {

    /**
     * DER? pkcs7 x509 など
     *
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public static byte[] binRead(String path) throws IOException {
        return binRead(new File(path));
    }
    
    public static byte[] binRead(File file) throws IOException {
        byte[] data;
        data = new byte[(int) file.length()];
        System.out.println(file.length());
        try (InputStream in = new FileInputStream(file)) {
            int o = in.read(data);
            System.out.println(o);
            in.close();
        }
        return data;
    }

    /**
     * 
     * @param in
     * @param out
     * @return
     * @throws IOException 
     */
    public static int io(InputStream in, OutputStream out) throws IOException {
        byte[] data = new byte[10200];
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
}
