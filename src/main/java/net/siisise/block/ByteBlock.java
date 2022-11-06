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
 * ブロック.
 * バイト列は複製しない.
 * 部分集合にも対応する。
 * add, delはできない。
 * ByteBuffer 相当.
 */
public class ByteBlock extends OverBlock.AbstractSubOverBlock {

    /**
     * 参照のみできる配列.
     */
    private final byte[] block;

    /**
     * 配列全体.
     * @param src 
     */
    public ByteBlock(byte[] src) {
        super(0,src.length);
        block = src;
    }
    
    /**
     * 配列の部分集合.
     * @param src 更新しない前提の配列.
     * @param start 開始位置
     * @param length サイズ
     */
    public ByteBlock(byte[] src, long start, long length) {
        super(start,start + length);
        block = src;
    }

    @Override
    public int read() {
        return ( pos >= max) ? -1 : block[(int)pos++] & 0xff;
    }
    
    /**
     * 指定サイズの部分集合を作る.
     * offsetは読み込んだ分進む.
     * @param size サイズ
     * @return 部分集合 subblock
     */
    @Override
    public ByteBlock readBlock(int size) {
        size = (int)Math.min( max - pos, size );
        ByteBlock b = new ByteBlock(block, pos, size);
        pos += size;
        return b;
    }

    /**
     * 
     * @param dst
     * @param offset
     * @param length
     * @return 
     */
    @Override
    public int read(byte[] dst, int offset, int length) {
        if ( offset < 0 || offset > dst.length || length < 0 ) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        // dataサイズと小さい方
        long size = Math.min(dst.length - offset, length);
        int p = (int)pos;
        size = skip(size);
        System.arraycopy(block, p, dst, offset, (int) size);
        return (int) size;
    }

    @Override
    public int backRead() {
        if ( pos > min ) {
            return block[(int)--pos] & 0xff;
        }
        return -1;
    }

    @Override
    public int backRead(byte[] dst, int offset, int length) {
        if ( offset < 0 || offset >= dst.length || length < 0 ) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        length = Math.min( dst.length - offset, length );
        int size = (int)back(length);
        System.arraycopy(block, (int)pos, dst, offset, size);
        return size;
    }

    /**
     * position から残りを position と limit に設定したByteBuffer
     * @return 
     */
    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(block, (int)pos, (int)(max - pos));
    }

    @Override
    public IndexInput get(long index, byte[] b, int offset, int length) {
        if ( 0 > index ) {
            throw new java.nio.BufferUnderflowException();
        }
        if ( index + length > length()) {
            throw new java.nio.BufferOverflowException();
        }
        System.arraycopy(block, (int)(min + index), b, offset, length);
        return this;
    }

    @Override
    public void put(long index, byte[] d, int offset, int length) {
        if ( 0 > index ) {
            throw new java.nio.BufferUnderflowException();
        }
        if ( index + length > length()) {
            throw new java.nio.BufferOverflowException();
        }
        System.arraycopy(d, offset, block, (int)(min + index), length);
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        if ( pos + length > length()) {
            throw new java.nio.BufferOverflowException();
        }
        System.arraycopy(data, offset, block, (int)(min + pos), length);
    }
}
