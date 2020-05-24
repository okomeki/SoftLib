package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * flushとcloseは持っていない、Exceptionも発生しない。
 * InputStream, OutputStream と少し仕様/動作が異なる。
 */
public interface BackPacket {

    OutputStream getOutputStream();
    InputStream getBackInputStream();

    void write(int data);
    void write(byte[] data, int offset, int length);
    void write(byte[] data);

    int backRead();
    int backRead(byte[] data, int offset, int length);
    int backRead(byte[] data);
 
}
