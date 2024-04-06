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

import java.nio.ByteBuffer;
import net.siisise.math.Matics;

/**
 * limit が変えられない ByteBuffer っぽい.
 *
 */
public class ByteBufferBlock extends OverBlock.AbstractSubOverBlock {

    private final ByteBuffer buff;

    /**
     * バイト列をByteBufferにしてByteBlockにする二重構造.
     * 仮なので使わないかも.
     *
     * @param src バイト列
     */
    public ByteBufferBlock(byte[] src) {
        super(0, src.length);
        buff = ByteBuffer.wrap(src);
    }

    /**
     * src の 0 から limit までを共有する。
     * position も同一.
     *
     * @param src Buffer
     */
    public ByteBufferBlock(ByteBuffer src) {
        super(0, src.limit());
        buff = src;
    }

    /**
     * 配列はお持ちですか.
     * ByteBufferの答えを返す.
     * @return たぶんはい
     */
    @Override
    public boolean hasArray() {
        return buff.hasArray();
    }

    @Override
    public byte[] array() {
        return buff.array();
    }

    @Override
    public int arrayOffset() {
        return buff.arrayOffset();
    }

    /**
     * 読む
     *
     * @param dst バッファ
     * @param offset バッファ位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    @Override
    public int read(byte[] dst, int offset, int length) {
        int size = buff.remaining();
        length = dst.length - offset > length ? length : dst.length - offset;
        size = (size < length) ? size : length;
        buff.get(dst, offset, length);
        return size;
    }

    @Override
    public OverBlock sub(long index, long length) {
        if (!Matics.sorted(0, index, index + length, max - min)) {
            throw new java.nio.BufferOverflowException();
        }
        if (buff.hasArray()) {
            return new ByteBlock(buff.array(), buff.arrayOffset() + min + index, length);
        }
        int p = buff.position();
        buff.position((int) index);
        ByteBuffer bb = buff.slice();
        bb.limit((int) length);
        buff.position(p);
        return new ByteBufferBlock(bb);
    }

    /**
     * 残り remaining() size() 分を配列にする
     *
     * @return 配列
     */
    @Override
    public byte[] toByteArray() {
        byte[] tmp = new byte[buff.remaining()];
        buff.get(tmp);
        return tmp;
    }

    @Override
    public int backRead() {
        int p = buff.position();
        if (p < 1) {
            return -1;
        }
        p--;
        buff.position(p);
        return buff.get() & 0xff;
    }

    @Override
    public int backRead(byte[] dst, int offset, int length) {
        int p = buff.position();
        int size = p < length ? p : length;
        buff.position(p - size);
        buff.get(dst, offset + length - size, size);
        buff.position(p - size);
        return size;
    }

    @Override
    public long length() {
        return buff.remaining();
    }

    /**
     * position がほしいか たぶん0からの位置
     *
     * @return たぶん0からの位置
     */
    @Override
    public long backLength() {
        return buff.position();
    }

    /**
     * 範囲内で移動する.
     * ReadableBlockと同じ.
     *
     * @param position
     * @return 位置.
     */
    @Override
    public long seek(long position) {
        int p = (int) Matics.range(position, 0, buff.limit());
        buff.position(p);
        return p;
    }

    /**
     * 読み書きせずに進む.
     *
     * @param length マイナスも使えるといい
     * @return 進んだサイズ
     */
    @Override
    public long skip(long length) {
        int p = buff.position();
        int mp = (int) Matics.range(length, -p, buff.remaining());
        buff.position(p + mp);
        return mp;
    }

    /**
     * 読み書きせずに戻る.
     *
     * @param length マイナスも使えるといい
     * @return 戻ったサイズ
     */
    @Override
    public long back(long length) {
        int p = buff.position();
        int mp = (int) Matics.range(length, -buff.remaining(), p);
        buff.position(p - mp);
        return mp;
    }

    @Override
    public OverBlock get(long index, byte[] b, int offset, int length) {
        int p = buff.position();
        buff.position((int) index);
        buff.get(b, offset, length);
        buff.position(p);
        return this;
    }

    /**
     * buffの上限サイズまで書ける、length が size() を超えると何かのエラー
     *
     * @param d データ
     * @param offset 位置
     * @param length サイズ
     */
    @Override
    public void write(byte[] d, int offset, int length) {
        buff.put(d, offset, length);
    }
}
