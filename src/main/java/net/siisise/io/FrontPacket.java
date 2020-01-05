package net.siisise.io;

import java.io.InputStream;

/**
 *
 */
public interface FrontPacket {
    
    InputStream getInputStream();

    int read();
    int read(byte[] data, int offset, int length);
    int read(byte[] data);

    byte[] toByteArray();

    void backWrite(int data);
    void backWrite(byte[] data, int offset, int length);
    void backWrite(byte[] data);
}
