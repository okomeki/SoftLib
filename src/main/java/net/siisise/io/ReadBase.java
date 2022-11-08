/*
 * Copyright 2022 okome.
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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 *
 */
public abstract class ReadBase implements FrontInput, IndexInput, RevInput, ReadableByteChannel {

    @Override
    public InputStream getInputStream() {
        return new FilterInput(this);
    }

    @Override
    public InputStream getBackInputStream() {
        return new RevInputStream(this);
    }

    @Override
    public byte get() {
        byte[] b = new byte[1];
        get(b,0,1);
        return b[0];
    }
    
    @Override
    public ReadBase get(byte[] b) {
        return get(b, 0, b.length);
    }
    
    @Override
    public ReadBase get(byte[] b, int offset, int length) {
        if ( length() < length ) {
            throw new java.nio.BufferOverflowException();
        }
        read(b,offset,length);
        return this;
    }

    @Override
    public byte get(long index) {
        byte[] d = new byte[1];
        get(index, d, 0, 1);
        return d[0];
    }

    @Override
    public IndexInput get(long index, byte[] b) {
        return get(index, b, 0, b.length);
    }

    @Override
    public int read() {
        byte[] d = new byte[1];
        return read(d,0,1) < 1 ? -1 : d[0] & 0xff;
    }

    @Override
    public int read(byte[] d) {
        return read(d, 0, d.length);
    }
    
    @Override
    public int read(ByteBuffer dst) {
        if ( dst.hasArray() ) {
            int p = dst.position();
            int r = dst.remaining();
            int s = read(dst.array(), dst.arrayOffset() + p, r);
            dst.position(p+s);
            return s;
        }
        byte[] d = new byte[dst.remaining()];
        int s = read(d);
        dst.put(d, 0, s);
        return s;
    }

    @Override
    public byte[] toByteArray() {
        byte[] b = new byte[size()];
        read(b);
        return b;
    }
    
    @Override
    public long skip(long length) {
        return Input.skipImpl(this, length);
    }
    
    /**
     * backSkip
     * @param length
     * @return 
     */
    @Override
    public long back(long length) {
        return RevInput.backImpl(this, length);
    }

    /**
     * PacketAを使った簡易実装.
     *
     * @param length
     * @return
     */
    @Override
    public Packet split(long length) {
        PacketA pac = new PacketA();
        pac.write(this, length);
        return pac;
    }

    @Override
    public int size() {
        return (int)Math.min(length(), Integer.MAX_VALUE);
    }

    @Override
    public byte revGet() {
        byte[] d = new byte[1];
        revGet(d, 0, 1);
        return d[0];
    }
    
    public ReadBase revGet(byte[] d) {
        return revGet(d, 0, d.length);
    }
    
    public ReadBase revGet(byte[] d, int offset, int length) {
        if ( backSize() < length ) {
            throw new java.nio.BufferUnderflowException();
        }
        backRead(d, offset, length);
        return this;
    }

    @Override
    public int backRead() {
        byte[] d = new byte[1];
        return backRead(d,0,1) < 1 ? -1 : d[0] & 0xff;
    }

    @Override
    public int backRead(byte[] data) {
        return backRead(data, 0, data.length);
    }

    @Override
    public Packet backSplit(long length) {
        Packet bp = new PacketA();
        RevOutput.backWrite(bp, this, length);
        return bp;
    }
}