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
 * ブロック.
 * バイト列は複製しない.
 * 部分集合にも対応する。
 * ByteBuffer 相当.
 */
public class ByteBlock extends SubReadableBlock {

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
    public ByteBlock(byte[] src, int start, int length) {
        super(start,start + length);
        block = src;
        pos = start;
    }

    @Override
    public int read() {
        return ( pos >= max) ? -1 : block[pos++] & 0xff;
    }
    
    /**
     * 指定サイズの部分集合を作る.
     * offsetは読み込んだ分進む.
     * @param size サイズ
     * @return 部分集合 subblock
     */
    @Override
    public ByteBlock readBlock(int size) {
        size = Integer.min( max - pos, size );
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
        int size = Integer.min(dst.length - offset, length);
        int p = pos;
        size = skip(size);
        System.arraycopy(block, p, dst, offset, size);
        return size;
    }

    @Override
    public int backRead() {
        if ( pos > min ) {
            return block[--pos] & 0xff;
        }
        return -1;
    }

    @Override
    public int backRead(byte[] dst, int offset, int length) {
        if ( offset < 0 || offset >= dst.length || length < 0 ) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        length = Integer.min( dst.length - offset, length );
        int size = back(length);
        System.arraycopy(block, pos, dst, offset, size);
        return size;
    }

    /**
     * position から残りを position と limit に設定したByteBuffer
     * @return 
     */
    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(block, pos, max - pos);
    }
}
