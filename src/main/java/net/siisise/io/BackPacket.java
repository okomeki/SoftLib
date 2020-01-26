package net.siisise.io;

import java.io.OutputStream;

public interface BackPacket {

    OutputStream getOutputStream();

    void write(int data);
    void write(byte[] data, int offset, int length);
    void write(byte[] data);

    int backRead();
    int backRead(byte[] data, int offset, int length);
    int backRead(byte[] data);
 
}
