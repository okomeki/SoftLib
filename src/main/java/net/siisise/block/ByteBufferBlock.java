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
import net.siisise.io.IndexInput;

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
        super(0,src.length);
        buff = ByteBuffer.wrap(src);
    }

    /**
     * src の 0 から limit までを共有する。
     * position も同一.
     * @param src Buffer
     */
    public ByteBufferBlock(ByteBuffer src) {
        super(0,src.limit());
        buff = src;
    }
    
    @Override
    public int read(byte[] dst, int offset, int length) {
        int size = buff.remaining();
        length = dst.length - offset > length ? length : dst.length - offset;
        size = (size < length) ? size : length;
        buff.get(dst, offset, length);
        return size;
    }

    @Override
    public ReadableBlock readBlock(int length) {
        length = Integer.min(size(), length);
        int pos = buff.position();
        ByteBuffer bb = buff.slice();
        bb.limit(length);
        buff.position(pos + length);
        return new ByteBufferBlock(bb);
    }

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
        buff.get(dst, offset, size);
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
    public int backSize() {
        return buff.position();
    }

    @Override
    public long seek(long po) {
        int p = (int)Math.min(buff.limit(), po);
        buff.position(p);
        return p;
    }

    @Override
    public long skip(long length) {
        if ( length < 0) {
            return -back(-length);
        }
        length = Long.min(size(),length);
        int p = buff.position();
        p += length;
        buff.position(p);
        return length;
    }

    @Override
    public long back(long length) {
        if ( length < 0 ) {
            return -skip(-length);
        }
        int p = buff.position();
        length = Long.min(p, length);
        p -= length;
        buff.position(p);
        return length;
    }

    @Override
    public IndexInput get(long index, byte[] b, int offset, int length) {
        int p = buff.position();
        buff.position((int)index);
        buff.get(b, offset, length);
        buff.position(p);
        return this;
    }

    @Override
    public void write(byte[] d, int offset, int length) {
        buff.put(d, offset, length);
    }

    @Override
    public void put(long index, byte[] d, int offset, int length) {
        int p = buff.position();
        buff.position((int)index);
        buff.put(d, offset, length);
        buff.position(p);
    }

}