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
import net.siisise.io.Packet;
import net.siisise.math.Matics;

/**
 * Editのテスト実装.
 */
public class SinglePacketBlock extends Edit implements EditBlock {

    private final Packet block;
    private long pos = 0;

    public SinglePacketBlock() {
        block = new PacketA();
    }

    public SinglePacketBlock(Packet p) {
        block = p;
    }

    public SinglePacketBlock(byte[] d) {
        block = new PacketA(d);
    }

    /**
     * 絶対位置移動.
     * 移動できる範囲で移動する.
     *
     * @param offset 絶対位置 absolute position
     * @return 新しい位置 new position
     */
    @Override
    public long seek(long offset) {
        return pos = Matics.range(offset, 0, block.length());
    }

    /**
     * 進む.
     * 移動できる範囲で移動する.
     *
     * @param length 相対位置
     * @return 進んだサイズ
     */
    @Override
    public long skip(long length) {
        long op = pos;
        pos = Matics.range(pos + length, 0, block.length());
        return pos - op;
    }

    /**
     * 戻る.
     * 移動できる範囲で移動する.
     *
     * @param length サイズ
     * @return 戻ったサイズ
     */
    @Override
    public long back(long length) {
        long op = pos;
        pos = Matics.range(pos - length, 0, block.length());
        return op - pos;
    }

    /**
     * 読めるサイズ long
     *
     * @return positionから後ろのサイズ
     */
    @Override
    public long length() {
        return block.length() - pos;
    }

    @Override
    public boolean readable(long length) {
        return block.readable(length + pos);
    }

    /**
     * 上書き.
     *
     * @param data ソース
     * @param offset source offset
     * @param length source length
     * @return これ
     */
    @Override
    public SinglePacketBlock put(byte[] data, int offset, int length) {
        block.put(pos, data, offset, length);
        pos += length;
        return this;
    }

    @Override
    public byte[] drop(int length) {
        byte[] d = new byte[Math.min(length, size())];
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

    /**
     * 上書き可能だが追加/削除された場合は保証されない
     *
     * @param length 長さ
     * @return 上書き可能な切り取り
     */
    @Override
    public OverBlock readBlock(long length) {
        length = Matics.range(length, 0, length());
        pos += length;
        return sub(pos - length, length);
    }

    /**
     * 仮でOverなかたち
     *
     * @return positionまでの切り取り
     */
    @Override
    public OverBlock flip() {
        return sub(0, block.backLength());
    }

    /**
     * 読む.
     *
     * @param index 位置
     * @param buf 入れ物
     * @param srcOffset dの位置
     * @param length 長さ
     * @return これ
     */
    @Override
    public SinglePacketBlock get(long index, byte[] buf, int srcOffset, int length) {
        block.get(index, buf, srcOffset, length);
        return this;
    }

    /**
     * 上書き.
     *
     * @param index 位置
     * @param d データ
     * @param offset データ位置
     * @param length 長さ
     */
    @Override
    public void put(long index, byte[] d, int offset, int length) {
        block.put(index, d, offset, length);
    }

    /**
     * 追加する.
     *
     * @param index 位置
     * @param d データ
     * @param offset data offset データ位置
     * @param length data length データ長さ
     */
    @Override
    public void add(long index, byte[] d, int offset, int length) {
        block.add(index, d, offset, length);
    }

    /**
     * 削除.
     *
     * @param index 位置
     * @param size 長さ
     */
    @Override
    public void del(long index, long size) {
        block.del(index, size);
    }

    /**
     * 削除.
     *
     * @param index delete position
     * @param buf buffer
     * @param offset buffer position
     * @param length delete length
     * @return これ
     */
    @Override
    public SinglePacketBlock del(long index, byte[] buf, int offset, int length) {
        block.del(index, buf, offset, length);
        return this;
    }

    /**
     * 読む.
     *
     * @param buf バッファ
     * @param offset バッファ位置
     * @param length 読みたい長さ
     * @return read length
     */
    @Override
    public int read(byte[] buf, int offset, int length) {
        length = (int) Math.min(length, length());
        block.get(pos, buf, offset, length); // 読んでも消えない
        pos += length;
        return length;
    }

    @Override
    public int backRead(byte[] buf, int offset, int length) {
        int size = Matics.range(length, 0, backSize());
        pos -= size;
        block.get(pos, buf, offset + length - size, size);
        return size;
    }

    @Override
    public long backLength() {
        return pos;
    }

    /**
     * write data.
     *
     * 仮で上限あり.
     *
     * @param d ソース
     * @param offset ソース位置
     * @param length 書き込む長さ
     */
    @Override
    public void backWrite(byte[] d, int offset, int length) {
        int size = Matics.range(length, 0, backSize());
        pos -= size;
        block.put(pos, d, offset + length - size, size);
    }

    /**
     * write data.
     *
     * 仮で上限あり.
     *
     * @param d ソース
     * @param offset ソース位置
     * @param length 書き込む長さ
     */
    @Override
    public void write(byte[] d, int offset, int length) {
        length = Matics.range(length, 0, size());
        block.put(pos, d, offset, length);
        pos += length;
    }

    /*
     * for Debug
     * @return debug info
     */
//    @Override
//   public String toString() {
//        return "pos:" + pos + " len: " + block.length();
//    }
}
