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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import net.siisise.math.Matics;

/**
 * ブロック.
 * バイト列は複製しない.
 * 部分集合にも対応する。
 * サイズは固定長.
 * add, delはできない。
 * ByteBuffer 相当.
 * ReadableBlock, OverBlock として最適かもしれない簡単実装.
 */
public class ByteBlock extends OverBlock.AbstractSubOverBlock {

    /**
     * データ配列.
     * min から maxの範囲でアクセスできる共有配列.
     */
    private final byte[] block;

    /**
     * 配列全体.
     * @param src 元データ 複製しない
     */
    public ByteBlock(byte[] src) {
        this(src, 0,src.length);
    }

    /**
     * 指定サイズの空のBlockを作る.
     * @param length サイズ
     */
    public ByteBlock(int length) {
        this(new byte[length], 0, length);
    }
    
    /**
     * 配列の部分集合.
     * @param src 元配列.
     * @param start 開始位置
     * @param length サイズ
     */
    public ByteBlock(byte[] src, long start, long length) {
        super(start,start + length);
        block = src;
    }

    /**
     * InputStreamとの違い
     * 入力ブロックせずに
     * データ:0-255 または データ無し:-1 を返す
     *
     * @return -1 または 0-255
     */
    @Override
    public int read() {
        return ( pos >= max) ? -1 : block[(int)pos++] & 0xff;
    }

    @Override
    public ByteBlock sub(long index, long length) {
        if ( !Matics.sorted(0, index, index + length, max - min)) {
            throw new java.nio.BufferOverflowException();
        }
        return new ByteBlock(block, min + index, length);
    }

    /**
     * 読む.
     * @param dst 転送先
     * @param offset 転送位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    @Override
    public int read(byte[] dst, int offset, int length) {
        if ( !Matics.sorted( 0, offset, offset + length, dst.length) ) {
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

    /**
     * 逆読み.
     * 短い場合は後ろから詰める.
     * @param dst バッファ
     * @param offset バッファ位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    @Override
    public int backRead(byte[] dst, int offset, int length) {
        if ( !Matics.sorted(0, offset, offset + length, dst.length) ) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        int size = (int)back(length);
        System.arraycopy(block, (int)pos, dst, offset + length - size, size);
        return size;
    }

    /**
     * position から残りを position と limit に設定したByteBuffer
     * @return メモリを共有したByteBuffer
     */
    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(block, (int)pos, (int)(max - pos));
    }

    @Override
    public ByteBlock get(long index, byte[] b, int offset, int length) {
        if ( !Matics.sorted(0, index, index + length, max - min ) ) {
            throw new java.nio.BufferOverflowException();
        }
        System.arraycopy(block, (int)(min + index), b, offset, length);
        return this;
    }

    @Override
    public void put(long index, byte[] d, int offset, int length) {
        if ( !Matics.sorted(0, index, index + length, max - min ) ) {
            throw new java.nio.BufferOverflowException();
        }
        System.arraycopy(d, offset, block, (int)(min + index), length);
    }

    /**
     * 書き込み.
     * 上限を超えるものはエラーにしておく
     * @param data データ
     * @param offset データ位置
     * @param length 末尾を超えないサイズ
     */
    @Override
    public void write(byte[] data, int offset, int length) {
        if ( pos + length > length()) {
            throw new java.nio.BufferOverflowException();
        }
        int size = Math.min(size(), length); // Exception 外す前提でとりあえず縮める
        System.arraycopy(data, offset, block, (int)pos, size);
        skip(size);
    }
    
    @Override
    public String toString() {
        try {
            return "min:" + min + " pos:"+ pos + " max:" + max + " " + new String(block, "utf-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return "min:" + min + " pos:"+ pos + " max:" + max;
    }
}
