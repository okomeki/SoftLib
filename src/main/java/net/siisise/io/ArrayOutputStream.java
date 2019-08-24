/*
 * ArrayOutputStream.java
 *
 * Created on 2007/03/11, 1:22
 */
package net.siisise.io;

import java.io.*;

/**
 * 固定サイズ配列へのStream
 *
 * @author 佐藤 雅俊 <okome@siisise.net>
 */
public class ArrayOutputStream extends OutputStream {

    final byte[] buff;
    int offset;

    public ArrayOutputStream(int size) {
        buff = new byte[size];
        offset = 0;
    }

    public ArrayOutputStream(byte[] data) {
        buff = data;
        offset = 0;
    }

    public ArrayOutputStream(byte[] data, int offset) {
        buff = data;
        this.offset = offset;
    }

    public void write(int d) throws IOException {
        try {
            buff[offset++] = (byte) d;
        } catch (java.lang.IndexOutOfBoundsException e) {
            throw new IOException(e);
        }
    }

    public void write(byte[] data, int offset, int length) throws IOException {
        try {
            System.arraycopy(data, offset, buff, this.offset, length);
            this.offset += length;
        } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
        }
    }

    /**
     * 複製しない
     */
    public byte[] toByteArray() {
        return buff;
    }

}
