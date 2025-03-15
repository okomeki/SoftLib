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

import java.io.IOException;
import java.io.InputStream;
import net.siisise.io.BackPacket;
import net.siisise.io.Edit;
import net.siisise.io.FrontPacket;
import net.siisise.io.IndexEdit;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.io.RevOutput;

/**
 * 編集点が中央になったPacket.
 * ソースとメモリ共有はされない.
 *
 * 先頭、終端と編集点を別にしたもの.
 */
public class PacketBlock extends Edit implements EditBlock {

    // 読み済み
    private final BackPacket front;
    private final FrontPacket back;

    public PacketBlock() {
        front = new PacketA();
        back = new PacketA();
    }

    /**
     * データ列から作るBlock データ列とこのPacketBlockは共有されない.
     *
     * @param data データ列
     */
    public PacketBlock(byte[] data) {
        this(new PacketA(data));
    }

    /**
     * 入力を繋ぐ.
     *
     * @param in 入力をFrontPacketでまとったもの
     */
    public PacketBlock(FrontPacket in) {
        front = new PacketA();
        back = in;
    }

    /**
     * inから読んでoutに出て行く形.
     * 全体サイズは不変.
     * position はfront側サイズを基準にする.
     *
     * @param in 処理前データ入れ back
     * @param out 処理後データ入れ front
     */
    public PacketBlock(FrontPacket in, BackPacket out) {
        this.front = out;
        this.back = in;
    }

    /**
     * 特定位置までposition移動.
     * 足りない場合は最後へ
     *
     * @param offset 位置
     * @return 移動した位置
     */
    @Override
    public long seek(long offset) {
        long fb = front.backLength();
        if (fb + back.length() < offset) {
            offset = fb + back.length();
        }
        long size = offset - fb;
        skip(size);
        return offset;
    }

    /**
     * 読みとばす。進む。
     *
     * @param length 長さ
     * @return skip length
     */
    @Override
    public long skip(long length) {
        if (length == 0) {
            return 0;
        } else if (length < 0) {
            return -back(-Math.max(length, -front.backLength()));
        }
        Packet fp = back.readPacket(length);
        long size = fp.length();
        front.write(fp);
        return size;
    }

    @Override
    public long back(long length) {
        if (length == 0) {
            return 0;
        } else if (length < 0) {
            return -skip(-Math.max(length, -back.length()));
        }
        Packet ff = front.backReadPacket(length);
        return RevOutput.backWrite(back, ff, ff.length());
    }

    /**
     * 編集可能なのでいろいろ違うかも
     *
     * @return position より前を切り取ったもの.
     */
    @Override
    public OverBlock flip() {
        return sub(0, backLength());
    }

    @Override
    public int read(byte[] data, int offset, int length) {
        int size = back.read(data, offset, length);
        front.write(data, offset, size);
        return size;
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        if (length > length()) {
            throw new java.nio.BufferOverflowException();
        }
        back.skip(length);
        front.write(data, offset, length);
    }

    /**
     * 直書き.
     * data列は再利用しないこと
     * 上限越えはエラー
     *
     * @param data 配列、データ
     */
    @Override
    public void dwrite(byte[] data) {
        if (data.length > length()) {
            throw new java.nio.BufferOverflowException();
        }
        back.skip(data.length);
        front.dwrite(data);
    }

    /**
     * 書き込む.
     *
     * @param pac 入力
     * @return 上書きしたサイズ
     */
    @Override
    public long write(Input pac) {
        long len = pac.length();
        back.skip(len);
        return front.write(pac);
    }

    @Override
    public long write(Input pac, long length) {
        if (length > pac.length()) {
            throw new java.nio.BufferOverflowException();
        }
        long len = Math.min(pac.length(), length);
        len = back.skip(len);
        return front.write(pac, len);
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

    /**
     * 読む.
     * 読んだところは消えない.
     *
     * @param index 位置
     * @param d データ入れ
     * @param offset d 位置
     * @param length 容量
     * @return これ
     */
    @Override
    public PacketBlock get(long index, byte[] d, int offset, int length) {
        long p = backLength();
        seek(index);
        get(d, offset, length);
        seek(p);
        return this;
    }

    /**
     * 上書き.読むデータがなくても追加する.
     * 有限サイズデータ(配列)の場合は上限まで書く?
     *
     * @param data データ列
     * @param offset 位置
     * @param length サイズ
     * @return 書き込めたサイズ
     */
    @Override
    public PacketBlock put(byte[] data, int offset, int length) {
        if (length > size()) {
            throw new java.nio.BufferOverflowException();
        }
        int size = (int) back.skip(length);
        front.write(data, offset, size);
        return this;
    }

    /**
     * 上書き.
     * 書き込み位置の制約はあまりない.
     *
     * @param index 位置
     * @param d データ
     * @param offset データ位置
     * @param length サイズ
     */
    @Override
    public void put(long index, byte[] d, int offset, int length) {
        long p = backLength();
        seek(index);
        put(d, offset, length);
        seek(p);
    }

    /**
     * 追加.
     * index位置に追加する.
     *
     * @param index block index
     * @param d data
     * @param offset data offset
     * @param length size
     */
    @Override
    public void add(long index, byte[] d, int offset, int length) {
        long p = backLength();
        seek(index);
        front.write(d, offset, length);
        seek(p);
    }

    /**
     * 削除.
     * index位置からsize分削除する.詰める.
     *
     * @param index block index
     * @param size delete size
     */
    @Override
    public void del(long index, long size) {
        long p = backLength();
        seek(index);
        back.readPacket(size);
        seek(p);
    }

    @Override
    public IndexEdit del(long index, byte[] d, int offset, int length) {
        long p = backLength();
        seek(index);
        back.read(d, offset, length);
        seek(p);
        return this;
    }

    @Override
    public InputStream getInputStream() {
        return new ReadableBlock.BlockInput(this);
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        if (backLength() < data.length) {
            throw new java.nio.BufferOverflowException();
        }
        front.back(length);
        back.backWrite(data, offset, length);
    }

    @Override
    public void dbackWrite(byte[] data) {
        if (backLength() < data.length) {
            throw new java.nio.BufferOverflowException();
        }
        front.back(data.length);
        back.dbackWrite(data);
    }

    @Override
    public long length() {
        return back.length();
    }

    @Override
    public boolean readable(long length) {
        return back.readable(length);
    }

    @Override
    public long backLength() {
        return front.backLength();
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        int size = Math.min(length, backSize());
        front.backRead(data, offset + length - size, size);
        back.backWrite(data, offset + length - size, size);
        return size;
    }

    @Override
    public void flush() {
        try {
            front.getOutputStream().flush();
        } catch (IOException ex) {
        }
        back.flush();
    }

    @Override
    public String toString() {
        return "PacketBlock size:" + size() + "position: " + backSize();
    }

    @Override
    public byte revGet() {
        int b = front.backRead();
        if (b < 0) throw new java.nio.BufferUnderflowException();
        back.backWrite(b);
        return (byte) b;
    }
}
