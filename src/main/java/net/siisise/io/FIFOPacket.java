/*
 * Copyright 2025 okome.
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

import java.io.InputStream;
import java.io.OutputStream;
import net.siisise.math.Matics;

/**
 * 片方向のみのリンクなので高速?
 */
public class FIFOPacket implements Input, Output {

    class Chain {

        byte[] data;
        Chain next;
    }

    int outoffset;
    // 入力点
    Chain in;
    Chain out;

    public FIFOPacket() {
        out = new Chain();
        in = out;
    }

    @Override
    public InputStream getInputStream() {
        return new FilterInput(this);
    }

    @Override
    public int read() {
        byte[] d = new byte[1];
        return read(d, 0, 1) < 1 ? -1 : d[0] & 0xff;
    }

    @Override
    public int read(byte[] buf, int offset, int length) {
        if (out.data == null) {
            return -1;
        }
        int len = length;
        while (out.data != null && out.data.length - outoffset <= len) {
            int rlen = out.data.length - outoffset;
            System.arraycopy(out.data, outoffset, buf, offset, rlen);
            len -= rlen;
            offset += rlen;
            out = out.next;
            outoffset = 0;
        }
        if (out.data != null && out.data.length - outoffset > len) {
            System.arraycopy(out.data, outoffset, buf, offset, len);
            outoffset += len;
            len = 0;
        }
        return length - len;
    }

    @Override
    public int read(byte[] d) {
        return read(d, 0, d.length);
    }

    @Override
    public byte get() {
        byte[] b = new byte[1];
        get(b, 0, 1);
        return b[0];
    }

    @Override
    public long get(byte[] b, int offset, int length) {
        if (!Matics.sorted(0, offset, offset + length, b.length) || length() < length) {
            throw new java.nio.BufferOverflowException();
        }
        return read(b, offset, length);
    }

    @Override
    public byte[] toByteArray() {
        byte[] tmp = new byte[size()];
        read(tmp);
        return tmp;
    }

    /**
     * 仮.
     *
     * @param length
     * @return
     */
    @Override
    public Packet readPacket(long length) {
        Packet pac = new PacketA();
        pac.write(this, length);
        return pac;
    }

    @Override
    public long skip(long length) {

        long len = length;
        while (out.data != null && out.data.length - outoffset <= len) {
            len -= out.data.length - outoffset;
            outoffset = 0;
            out = out.next;
        }
        if (out.data != null && out.data.length - outoffset < len) {
            outoffset += len;
            len = 0;
        }
        return length - len;
    }

    @Override
    public long length() {
        long len = -outoffset;
        Chain o = out;
        while (o.data != null) {
            len += o.data.length;
            o = o.next;
        }
        return len;
    }

    @Override
    public boolean readable(long length) {
        length += outoffset;
        Chain o = out;
        while (o.data != null && length > 0) {
            length -= o.data.length;
            o = o.next;
        }
        return length <= 0;
    }

    @Override
    public OutputStream getOutputStream() {
        return new FilterOutput(this);
    }

    @Override
    public void write(int data) {
        write(new byte[]{(byte) data});
    }

    @Override
    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        Chain n = new Chain();
        in.data = new byte[Math.min(length, data.length - offset)];
        System.arraycopy(data, offset, in.data, 0, in.data.length);
        Chain i = in;
        in = n;
        i.next = n;
    }

    @Override
    public void dwrite(byte[] data) {
        Chain n = new Chain();
        in.data = data;
        Chain i = in;
        in = n;
        i.next = n;
    }

    @Override
    public long write(Input pac) {
        byte[] tmp = new byte[0x1000000];
        int len;
        long xlen = 0;
        len = pac.read(tmp);
        while (len > 0) {
            xlen += len;
            write(tmp, 0, len);
            len = pac.read(tmp);
        }
        return xlen;
    }

    @Override
    public long write(Input pac, long length) {
        long len = Math.min(length, length());
        length = len;
        while (length > 0) {
            byte[] tmp = new byte[length > 0x1000000 ? 0x1000000 : (int) length];
            length -= tmp.length;
            pac.read(tmp);
            dwrite(tmp);
        }
        return len;
    }

    @Override
    public Output put(byte data) {
        return put(new byte[]{data});
    }

    @Override
    public Output put(byte[] data) {
        return put(data, 0, data.length);
    }

    @Override
    public Output put(byte[] data, int offset, int length) {
        write(data, offset, length);
        return this;
    }

}
