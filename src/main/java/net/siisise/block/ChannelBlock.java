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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * ランダム読み書きができる SeekableByteBlock に対応しておく.
 */
public class ChannelBlock extends OverBlock.AbstractOverBlock implements Closeable {

    private SeekableByteChannel ch;

    /**
     * 読む形で開く.
     * @param file ふぁいる
     * @return 読みclose できるBlock
     * @throws FileNotFoundException 
     */
    public static ReadableBlock wrap(File file) throws FileNotFoundException {
        RandomAccessFile io = new RandomAccessFile(file, "r");
        return new ChannelBlock(io.getChannel());
    }

    /**
     * 読み専用でBlockにする.
     * @param path nio な path
     * @return 読みclose できるBlock
     * @throws IOException 
     */
    public static ReadableBlock wrap(Path path) throws IOException {
        FileChannel c = FileChannel.open(path, StandardOpenOption.READ);
        return new ChannelBlock(c);
    }

    /**
     * 読み書きできる形のBlockで開く.
     * @param file ファイル
     * @return 読み書きclose できるBlock
     * @throws IOException 
     */
    public static OverBlock over(File file) throws IOException {
        RandomAccessFile io = new RandomAccessFile(file,"rw");
        return new ChannelBlock(io.getChannel());
    }

    public static OverBlock over(Path path) throws IOException {
        FileChannel c = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        return over(c);
    }

    public static OverBlock over(SeekableByteChannel ch) throws IOException {
        return new ChannelBlock(ch);
    }

    public ChannelBlock(SeekableByteChannel ch) {
        this.ch = ch;
    }

    /**
     * 先頭からの位置.
     * 読み書きした長さ.
     * 戻って読み書きするときなどに.
     * @return サイズ/位置
     */
    @Override
    public long backLength() {
        try {
            return ch.position();
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }

    /**
     * これから読み書きできる長さ.
     * @return 長さ
     */
    @Override
    public long length() {
        try {
            return ch.size() - ch.position();
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }

    /**
     * 移動.
     * @param offset 指定位置
     * @return 移動した位置
     */
    @Override
    public long seek(long offset) {
        try {
            ch.position(offset);
            return ch.position();
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }

    @Override
    public int read(byte[] d, int offset, int length) {
        try {
            return ch.read(ByteBuffer.wrap(d,offset,length));
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }
    
    @Override
    public ChannelBlock get(long index, byte[] b, int offset, int length) {
        try {
            if (ch.size() - index < length) {
                throw new java.nio.BufferOverflowException();
            }
            long p = ch.position();
            ch.position(index);
            read(b, offset, length);
            ch.position(p);
            return this;
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }

    /**
     * 逆から読む.
     * ToDo: 後ろから読むよう要修正?
     * @param buf
     * @param offset
     * @param length
     * @return 
     */
    @Override
    public int backRead(byte[] buf, int offset, int length) {
        try {
            long p = backLength();
            int nlength = (int)Math.min(p, length);
            int noff = offset + length - nlength;
            
            seek(p - nlength);
            ByteBuffer bb = ByteBuffer.wrap(buf, noff, nlength);
            int l = ch.read(bb);
            assert l == nlength;
            seek(p - nlength);
            return l;
        } catch (IOException ex) {
            throw new java.nio.BufferUnderflowException();
        }
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        try {
            ch.write(ByteBuffer.wrap(data, offset, length));
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }

    @Override
    public void close() throws IOException {
        ch.close();
        ch = null;
    }

}
