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
package net.siisise.block;

import net.siisise.io.Edit;
import net.siisise.io.PacketA;
import net.siisise.io.IndexEdit;
import net.siisise.io.Packet;

/**
 * Baseのテスト実装.
 */
public class SinglePacketBlock extends Edit implements EditBlock {
    
    private final Packet block;
    private long pos;
    
    public SinglePacketBlock() {
        block = new PacketA();
        pos = 0;
    }

    public SinglePacketBlock(Packet p) {
        block = p;
        pos = 0;
    }

    public SinglePacketBlock(byte[] d) {
        block = new PacketA(d);
        pos = 0;
    }

    @Override
    public long seek(long offset) {
        pos = Math.min(Math.max(0, offset), length());
        return pos;
    }

    @Override
    public long skip(long length) {
        if ( length == Long.MIN_VALUE ) {
            throw new java.nio.BufferUnderflowException();
        } else if ( length < 0 ) {
            return -back(-length);
        }
        long s = Math.min(length() - pos, length);
        pos += s;
        return s;
    }

    @Override
    public long back(long length) {
        if ( length == Long.MIN_VALUE ) {
            throw new java.nio.BufferOverflowException();
        } else if ( length < 0 ) {
            return -skip(-length);
        }
        long s = Math.min(pos, length);
        pos -= s;
        return s; 
    }

    @Override
    public long length() {
        return block.length() - pos;
    }

    /**
     * 上書き.
     * @param data
     * @param offset
     * @param length
     * @return 
     */
    @Override
    public SinglePacketBlock put(byte[] data, int offset, int length) {
        block.put(pos,data,offset,length);
        pos += length;
        return this;
    }

    @Override
    public byte[] drop(int length) {
        byte[] d = new byte[(int)Math.min(length(), length)];
        block.del(pos, d);
        return d;
    }

    @Override
    public byte[] backDrop(int length) {
        byte[] d = new byte[Math.min(backSize(), length)];
        block.del(pos - d.length, d);
        pos -= d.length;
        return d;
    }

    @Override
    public ReadableBlock readBlock(int length) {
        byte[] d = new byte[Math.min(length, size())];
        block.get(pos, d);
        pos += d.length;
        return ReadableBlock.wrap(d, 0, d.length);
    }

    @Override
    public OverBlock flip() {
        return new SubOverBlock(0,block.backSize(),this);
    }

    @Override
    public SinglePacketBlock get(long index, byte[] d, int srcOffset, int length) {
        block.get(index,d,srcOffset, length);
        return this;
    }

    @Override
    public void put(long index, byte[] d, int srcOffset, int length) {
        block.put(index,d,srcOffset, length);
    }

    @Override
    public void add(long index, byte[] d, int srcOffset, int length) {
        block.add(index,d,srcOffset, length);
    }

    @Override
    public void del(long index, long size) {
        block.del(index,size);
    }

    @Override
    public IndexEdit del(long index, byte[] d, int offset, int length) {
        block.del(index,d,offset,length);
        return this;
    }

    @Override
    public int read(byte[] d, int offset, int length) {
        length = (int)Math.min(length, length());
        block.get(pos, d, offset, length);
        pos += length;
        return length;
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        length = Math.min(length, backSize());
        pos -= length;
        block.get(pos, data, offset, length);
        return length;
    }

    @Override
    public int backSize() {
        return (int)Math.min(pos,Integer.MAX_VALUE);
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        length = Math.min(length, backSize());
        pos -= length;
        block.put(pos, data, offset, length);
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        length = (int)Math.min(length, length());
        block.put(pos, data, offset, length);
        pos += length;
    }
/*
    @Override
    public void dbackWrite(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dwrite(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/

}
