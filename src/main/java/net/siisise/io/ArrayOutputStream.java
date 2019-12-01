package net.siisise.io;

import java.io.*;

/**
 * 固定サイズ配列へのStream.
 *
 * Created on 2007/03/11, 1:22
 *
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

    @Override
    public void write(int d) throws IOException {
        try {
            buff[offset++] = (byte) d;
        } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
        }
    }

    @Override
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
     * @return 
     */
    public byte[] toByteArray() {
        return buff;
    }

}
