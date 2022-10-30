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
package net.siisise.pac;

import java.io.InputStream;
import java.io.OutputStream;
import net.siisise.io.FrontPacket;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;

/**
 * 編集点が中央になったPacket.
 * 
 * 先頭、終端と編集点を別にしたもの.
 * Packet を継承すると BackPacket系が混乱するので継承しない方がいいのかも.
 */
public class PacketBlock implements EditBlock {

    // 読み済み
    private final Packet front;
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
    public int seek(int offset) {
        if (front.size() + back.size() < offset) {
            offset = front.size() + back.size();
        }
        while (front.size() < offset) {
            int size = offset - front.size();
            if (size > 0x1000000) {
                size = 0x1000000;
            }
            readBlock(size);
        }
        while (front.size() > offset) {
            int size = front.size() - offset;
            if (size > 0x100000) {
                size = 0x100000;
            }
            byte[] tmp = new byte[size];
            backRead(tmp);
            
        }
        return offset;
    }
    
    @Override
    public int skip(int length) {
        if ( length < 0 ) {
            return -back(-length);
        }
        FrontPacket fp = back.split(length);
        int size = fp.size();
        front.write(fp);
        return size;
    }

    @Override
    public int back(int length) {
        byte[] d = new byte[length];

        int size = front.backRead(d);
        back.backWrite(d, 0, size);
        return size;
    }

    @Override
    public int read() {
        if (back.size() > 0) {
            int d = back.read();
            front.write(d);
            return d;
        }
        return -1;
    }

    @Override
    public int read(byte[] data) {
        int size = back.read(data);
        front.write(data, 0, size);
        return size;
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
        return new SubXReadableBlock(backSize(), backSize() + size, this);
    }

    /**
     * 切り取り.
     * 切り取った部分はなくなる。
     * @param length
     * @return 
     */
    @Override
    public FrontPacket split(int length) {
        return back.split(length);
    }

    @Override
    public int read(byte[] data, int offset, int length) {
        int size = back.read(data, offset, length);
        front.write(data, offset, size);
        return size;
    }

    @Override
    public void write(int b) {
        front.write(b);
    }

    @Override
    public void write(byte[] data) {
        front.write(data);
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
        int size = length > back.size() ? back.size() : length;
        byte[] tmp = new byte[size];
        back.read(tmp);
        return tmp;
    }
    
    @Override
    public byte[] backDrop(int length) {
        int size = length > front.size() ? front.size() : length;
        byte[] tmp = new byte[size];
        front.backRead(tmp);
        return tmp;
    }
    
    @Override
    public int overWrite(int data) {
        return overWrite(new byte[] {(byte)data});
    }

    @Override
    public int overWrite(byte[] data) {
        return overWrite(data, 0, data.length);
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
    public int overWrite(byte[] data, int offset, int length) {
        int size = back.read(new byte[length]);
        front.write(data, offset, size);
        return size;
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
    public byte[] toByteArray() {
        byte[] data = new byte[size()];
        read(data);
        return data;
    }

    @Override
    public void backWrite(int data) {
        back.backWrite(data);
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        back.backWrite(data, offset, length);
    }

    @Override
    public void backWrite(byte[] data) {
        back.backWrite(data);
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
    public int size() {
        return back.size();
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
    public InputStream getBackInputStream() {
        return front.getBackInputStream();
    }

    @Override
    public int backRead() {
        if ( front.length() == 0 ) {
            return -1;
        }
        int v = front.backRead();
        back.backWrite(v);
        return v;
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        int size = front.backRead(data, offset, length);
        back.backWrite(data, offset, size);
        return size;
    }

    @Override
    public int backRead(byte[] data) {
        int size = front.backRead(data);
        back.backWrite(data,0,size);
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
}