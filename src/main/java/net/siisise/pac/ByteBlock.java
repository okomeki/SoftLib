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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * ブロック.
 * バイト列は複製しない.
 */
public class ByteBlock implements ReadableBlock {
    
    private final byte[] block;
    
    private int offset;
    
    public ByteBlock(byte[] src) {
        block = src;
    }

    @Override
    public int read() {
        if ( offset >= block.length) {
            return -1;
        }
        return block[offset++] & 0xff;
    }

    @Override
    public int read(byte[] data) {
        return read(data,0, data.length);
    }

    /**
     * 
     * @param data
     * @param offset
     * @param length
     * @return 
     */
    @Override
    public int read(byte[] data, int offset, int length) {
        int size = block.length - this.offset;
        if ( length < size) { // 最大 と 指定サイズの小さい方
            size = length;
        }
        if ( data.length < offset + size ) { // dataサイズと小さい方
            size = data.length - offset;
        }
        System.err.println("block offset:" + this.offset + " offset:" + offset + " size: " + size);
        System.arraycopy(block, this.offset, data, offset, size);
        this.offset += size;
        return size;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    /**
     * 
     * @param offset
     * @return 
     */
    @Override
    public int seek(int offset) {
        if (offset < block.length) {
            this.offset = offset;
        } else {
            this.offset = block.length;
        }
        return this.offset;
    }

    /**
     * 
     * @param length マイナスも使えるといい
     * @return 
     */
    @Override
    public int skip(int length) {
        if ( length < 0) { // back
            if ( offset >= -length) {
                offset += length;
                return length;
            } else {
                int size = offset;
                offset = 0;
                return size;
            }
        }
        int size = block.length - offset > length ? length : (block.length - offset);
        offset += size;
        return size;
    }

    @Override
    public int back(int length) {
        if ( length <= offset ) {
            offset -= length;
        } else {
            length = offset;
            offset = 0;
        }
        return length;
    }

    /**
     * Packet と互換にすること.
     * @return 読んでいない部分のみ.
     */
    @Override
    public byte[] toByteArray() {
        byte[] tmp = new byte[block.length - offset];
        read(tmp);
        return tmp;
    }

    @Override
    public long length() {
        return block.length - offset;
    }

    @Override
    public int size() {
        return block.length - offset;
    }

    /**
     * 仮対応.
     * まだまともには使えない
     * @return 仮
     */
    @Override
    public InputStream getInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(block);
        in.skip(offset);
        return in;
    }

    @Override
    public InputStream getBackInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int backRead() {
        if ( offset > 0 ) {
            return block[--offset] & 0xff;
        }
        return -1;
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        int size = this.offset > length ? length : this.offset;
        if ( data.length - offset < size) {
            size = data.length - offset;
        }
        this.offset -= size;
        System.arraycopy(block, this.offset, data, offset, size);
        return size;
    }

    @Override
    public int backRead(byte[] data) {
        return backRead(data, 0, data.length);
    }

}
