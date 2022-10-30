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

import java.nio.ByteBuffer;

/**
 *
 */
public class ByteBufferBlock extends AbstractReadableBlock {

    private final ByteBuffer buff;

    public ByteBufferBlock(byte[] src) {
        buff = ByteBuffer.wrap(src);
    }

    public ByteBufferBlock(ByteBuffer src) {
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
        return size();
    }

    @Override
    public int size() {
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
    public int seek(int po) {
        int p = buff.limit() > po ? po : buff.limit();
        buff.position(p);
        return p;
    }

    @Override
    public int skip(int length) {
        int p = buff.position();
        if (buff.limit() < p + length) {
            length = size();
        }
        p += length;
        buff.position(p);
        return length;
    }

    @Override
    public int back(int length) {
        int p = buff.position();
        if (p < length) {
            length = p;
        }
        p -= length;
        buff.position(p);
        return length;
    }

}
