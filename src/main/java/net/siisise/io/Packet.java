package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author okome
 */
public interface Packet {
    InputStream getInputStream();
    OutputStream getOutputStream();
    
    int read();
    int read(byte[] b, int offset, int length);
    int read(byte[] b);
    byte[] toByteArray();
    
    void write(int b);
    void write(byte[] b, int offset, int length);
    void write(byte[] b);
    
    int backRead();
    int backRead(byte[] b, int offset, int length);
    int backRead(byte[] b);
    
    void backWrite(int b);
    void backWrite(byte[] b, int offset, int length);
    void backWrite(byte[] b);
    
    long length();
}
