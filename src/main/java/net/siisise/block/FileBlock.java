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
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.siisise.io.IndexInput;

/**
 * テスト実装.
 * limitはない.
 */
public class FileBlock extends OverBlock.AbstractSubOverBlock implements Closeable {

    private final FileChannel ch;

    FileBlock(File file, String m) throws FileNotFoundException {
        super(0, file.length());
        RandomAccessFile io = new RandomAccessFile(file, m);
        ch = io.getChannel();
    }
    
    FileBlock(FileChannel c) throws IOException {
        super(0, c.size());
        ch = c;
    }

    public static ReadableBlock wrap(File file) throws FileNotFoundException {
        return new FileBlock(file, "r");
    }

    public static ReadableBlock wrap(Path path) throws IOException {
        FileChannel c = FileChannel.open(path, StandardOpenOption.READ);
        return new FileBlock(c);
    }

    public static OverBlock over(File file) throws IOException {
        RandomAccessFile io = new RandomAccessFile(file,"rw");
        return new FileBlock(io.getChannel());
    }

    public static OverBlock over(Path path) throws IOException {
        FileChannel c = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        return over(c);
    }

    public static OverBlock over(FileChannel ch) throws IOException {
        return new FileBlock(ch);
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
    public IndexInput get(long index, byte[] b, int offset, int length) {
        try {
            long p = ch.position();
            ch.position(index);
            if (ch.size() - index < length) {
                throw new java.nio.BufferOverflowException();
            }
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
     * @param data
     * @param offset
     * @param length
     * @return 
     */
    @Override
    public int backRead(byte[] data, int offset, int length) {
        try {
            long p = ch.position();
            ch.position(p - length);
            return ch.read(ByteBuffer.wrap(data, offset, length));
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
    public void put(long index, byte[] d, int offset, int length) {
        try {
            long p = ch.position();
            ch.position(index);
            ch.write(ByteBuffer.wrap(d, offset, length));
            ch.position(p);
        } catch (IOException ex) {
            throw new java.nio.BufferOverflowException();
        }
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

}
