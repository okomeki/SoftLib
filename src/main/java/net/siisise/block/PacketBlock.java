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

import java.io.InputStream;
import java.io.OutputStream;
import net.siisise.io.Edit;
import net.siisise.io.FrontPacket;
import net.siisise.io.IndexEdit;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.io.RevOutput;

/**
 * 編集点が中央になったPacket.
 * 
 * 先頭、終端と編集点を別にしたもの.
 * Packet を継承すると BackPacket系が混乱するので継承しない方がいいのかも.
 */
public class PacketBlock extends Edit implements EditBlock {

    // 読み済み
    private final PacketA front;
    private final FrontPacket back;

    public PacketBlock() {
        front = new PacketA();
        back = new PacketA();
    }

    public PacketBlock(byte[] data) {
        front = new PacketA();
        back = new PacketA(data);
    }

    public PacketBlock(FrontPacket in) {
        front = new PacketA();
        back = in;
    }

    @Override
    public long seek(long offset) {
        if (front.backSize() + back.size() < offset) {
            offset = front.backSize() + back.size();
        }
        long size = offset - front.backSize();
        skip(size);
        return offset;
    }

    @Override
    public long skip(long length) {
        if ( length < 0 ) {
            return -back(-length);
        }
        FrontPacket fp = back.split(length);
        int size = fp.size();
        front.write(fp);
        return size;
    }

    @Override
    public long back(long length) {
        long fss = front.backSize();
        long bs = Long.min(fss, length);
        
        Packet ff = front.backSplit(bs);
        return RevOutput.backWrite(back, ff, ff.length());
    }

    /**
     * 複製または参照を作る.
     * 必ずコピーされるわけではない.
     * @param size 必要な切り取りサイズ
     * @return 切り取られたサイズのブロック.
     */
    @Override
    public ReadableBlock readBlock(int size) {
        size = Integer.min(size(), size);
        FrontPacket p = back.split(size);
        front.write(p);
        int fs = front.size();
        return new SubReadableBlock(fs - size, fs, this);
    }

    /**
     * 切り取り.
     * 切り取った部分はなくなる。 (仮
     * @param length
     * @return 
     */
    @Override
    public Packet split(long length) {
        return back.split(length);
    }
    
    /**
     * 編集可能なのでいろいろ違うかも
     * @return 
     */
    @Override
    public OverBlock flip() {
        return new SubOverBlock(0, backSize(), this);
    }

    @Override
    public int read(byte[] data, int offset, int length) {
        int size = back.read(data, offset, length);
        front.write(data, offset, size);
        return size;
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        front.write(data, offset, length);
    }

    @Override
    public void dwrite(byte[] data) {
        front.dwrite(data);
    }

    @Override
    public void write(Input pac) {
        front.write(pac);
    }

    @Override
    public byte[] drop(int length) {
        length = Math.min(length, size());
        byte[] tmp = new byte[length];
        back.skip(length);
        return tmp;
    }

    @Override
    public byte[] backDrop(int length) {
        length = Math.min(length, backSize());
        byte[] tmp = new byte[length];
        front.backRead(tmp);
        return tmp;
    }
    
    @Override
    public PacketBlock get(long index, byte[] d, int offset, int length) {
        int p = backSize();
        seek(index);
        get(d, offset, length);
        seek(p);
        return this;
    }

    /**
     * 上書き.読むデータがなくても追加する.
     * 有限サイズデータ(配列)の場合は上限まで書く?
     * @param data データ列
     * @param offset 位置
     * @param length サイズ
     * @return 書き込めたサイズ
     */
    @Override
    public PacketBlock put(byte[] data, int offset, int length) {
        if ( length > size() ) {
            throw new java.nio.BufferOverflowException();
        }
        int size = (int)back.skip(length);
        front.write(data, offset, size);
        return this;
    }
    
    @Override
    public void put(long index, byte[] d, int offset, int length) {
        int p = backSize();
        seek(index);
        put(d,offset,length);
        seek(p);
    }

    @Override
    public void add(long index, byte[] d, int offset, int length) {
        int p = backSize();
        seek(index);
        front.write(d,offset,length);
        seek(p);
    }

    @Override
    public void del(long index, long size) {
        int p = backSize();
        seek(index);
        back.split(size);
        seek(p);
    }

    @Override
    public IndexEdit del(long index, byte[] d, int offset, int length) {
        int p = backSize();
        seek(index);
        back.read(d,offset,length);
        seek(p);
        return this;
    }

    @Override
    public InputStream getInputStream() {
        return back.getInputStream();
    }

    @Override
    public OutputStream getBackOutputStream() {
        return back.getBackOutputStream();
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        back.backWrite(data, offset, length);
    }

    @Override
    public void dbackWrite(byte[] data) {
        back.dbackWrite(data);
    }

    @Override
    public long length() {
        return back.length();
    }

    @Override
    public int backSize() {
        return front.size();
    }

    @Override
    public OutputStream getOutputStream() {
        return front.getOutputStream();
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        int size = front.backRead(data, offset, length);
        back.backWrite(data, offset, size);
        return size;
    }

    @Override
    public void flush() {
        front.flush();
        back.flush();
    }
    
    @Override
    public String toString() {
        return "PacketBlock size:" + size() + "position: " + backSize();
    }

    @Override
    public byte revGet() {
        int b = front.backRead();
        if ( b < 0 ) throw new java.nio.BufferUnderflowException();
        back.backWrite(b);
        return (byte)b;
    }
}
