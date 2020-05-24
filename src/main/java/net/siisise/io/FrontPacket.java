package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Packet と InputStream の共通のものにしたい
 */
public interface FrontPacket {
    
    /**
     * ストリームと完全互換ではないがそれっぽくしてくれる.
     * @return 
     */
    InputStream getInputStream();
    OutputStream getBackOutputStream();

    int read();
    int read(byte[] data, int offset, int length);
    int read(byte[] data);

    byte[] toByteArray();

    void backWrite(int data);
    void backWrite(byte[] data, int offset, int length);
    void backWrite(byte[] data);

    long length();
    int size();
}
