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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import net.siisise.io.FilterInput;
import net.siisise.io.FrontPacket;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.io.ReadBase;
import net.siisise.io.RevInput;
import net.siisise.io.RevOutput;
import net.siisise.io.StreamFrontPacket;

/**
 * Buffer の読み込み専用 っぽいものをStream風メソッドで実装したもの.
 * position() は backSize()
 */
public interface ReadableBlock extends Block, Input, RevInput {

    /**
     * 現在値から部分的な切り出し.
     * メモリ空間は可能な場合共有する.
     *
     * @param length
     * @return 部分要素.
     */
    ReadableBlock readBlock(int length);

    public static ReadableBlock wrap(String s) {
        return new ByteBlock(s.getBytes(StandardCharsets.UTF_8));
    }

    public static ReadableBlock wrap(byte[] b) {
        return new ByteBlock(b);
    }

    public static ReadableBlock wrap(byte[] b, int offset, int length) {
        return new ByteBlock(b, offset, length);
    }

    public static ReadableBlock wrap(File file) throws FileNotFoundException {
        return FileBlock.readBlock(file);
    }

    /**
     * 使いやすそうなのでラップする.
     *
     * @param bb ByteBuffer
     * @return ReadableBlock
     */
    public static ReadableBlock wrap(ByteBuffer bb) {
        return new ByteBufferBlock(bb);
    }

    public static ReadableBlock wrap(FrontPacket pac) {
        return new PacketBlock(pac);
    }

    /**
     * InputStreamからの変換. 最後まで使ったときはcloseする. 途中の場合はcloseしない.
     *
     * @param in 入力
     * @return
     */
    public static ReadableBlock wrap(InputStream in) {
        return wrap(new StreamFrontPacket(in));
    }

    public static ReadableBlock wrap(Reader in) {
        return wrap(new StreamFrontPacket(in));
    }

    public static ReadableBlock wrap(Input in) {
        return wrap(in.getInputStream());
    }
/**
 * 一般的なところだけ載せる.
 */
public abstract class AbstractReadableBlock extends ReadBase implements ReadableBlock, Iterator<Byte> {

    @Override
    public InputStream getInputStream() {
        return new FilterInput(this) {
            int mark = -1;

            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public void mark(int readlimit) {
                mark = AbstractReadableBlock.this.backSize();
            }

            @Override
            public void reset() {
                if (mark >= 0) {
                    seek(mark);
                }
            }
        };
    }

    @Override
    public long seek(long offset) {
        offset = 0 > offset ? 0 : offset;
        offset = Math.min(offset, backSize() + size());
        long size = offset - backSize();
        if ( size > 0 ) {
            skip(size);
        } else {
            back(-size);
        }
        return offset;
    }

    @Override
    public ReadableBlock readBlock(int size) {
        size = Integer.min(size, size());
        ReadableBlock nb = new SubReadableBlock(backSize(), backSize() + size, this);
        skip(size);
        return nb;
    }   

    @Override
    public AbstractReadableBlock get(long index, byte[] d, int offset, int length) {
        int p = backSize();
        seek(index);
        get(d, offset, length);
        seek(p);
        return this;
    }

    /**
     * ここまでを切り出すが、ポインタは移動しない。
     * @return 
     */
    @Override
    public ReadableBlock flip() {
        return new SubReadableBlock(0, backSize(), this);
    }

    /**
     * 仮
     * @param length
     * @return 
     */
    @Override
    public Packet backSplit(long length) {
        length = Long.min(length, backSize());
        PacketA pac = new PacketA();
        RevOutput.backWrite(pac, this, length);
        return pac;
    }

    class BytesIterator implements Iterator<byte[]> {
        
        int len;
        
        BytesIterator(int size) {
            len = size;
        }

        @Override
        public boolean hasNext() {
            return length() >= len;
        }

        @Override
        public byte[] next() {
            byte[] d = new byte[len];
            read(d);
            return d;
        }
    
    }
    
    /**
     * position からの Iterator (べつ?
     * @param len
     * @return 
     */
    public Iterator<byte[]> iterator(int len) {
        return new BytesIterator(len);
    }
    
    public Iterator<Byte> iterator() {
        return this;
    }
    
    @Override
    public boolean hasNext() {
        return length() > 0;
    }

    @Override
    public Byte next() {
        int v = read();
        return (byte)v;
    }
}

    /**
     * 部分集合用の軽い実装.
     */
    public static abstract class AbstractSubReadableBlock extends AbstractReadableBlock {

        /**
         * 最小位置. java.nio.Buffer の arrayOffset()
         */
        protected final long min;
        /**
         * 最大位置. block.length の代わり java.nio.Buffer の limit
         */
        protected final long max;
        protected long pos;

        /**
         * pos は未指定
         *
         * @param min
         * @param max
         */
        protected AbstractSubReadableBlock(long min, long max) {
            this.min = pos = min;
            this.max = max;
        }

        /**
         * 範囲内で移動する.
         *
         * @param position
         * @return 位置.
         */
        @Override
        public long seek(long position) {
            if (position + min >= max) {
                pos = max;
            } else if (position > 0) {
                pos = min + position;
            } else {
                pos = min;
            }

            return pos - min;
        }

        /**
         *
         * @param length マイナスも使えるといい
         * @return
         */
        @Override
        public long skip(long length) {
            if (length < 0) {
                return -back(-Long.max(length, -backSize()));
            }
            int size = (int) Long.min(size(), (int) length);
            pos += size;
            return size;
        }

        @Override
        public long back(long length) {
            if (length < 0) {
                return -skip(-Long.max(length, -size()));
            }
            int size = (int) Long.min(backSize(), length);
            if (length <= pos - min) {
                pos -= length;
            } else {
                length = pos - min;
                pos = min;
            }
            return length;
        }

        @Override
        public long length() {
            return max - pos;
        }

        /**
         * position だったもの.
         *
         * @return 読み込み済みのサイズ position
         */
        @Override
        public int backSize() {
            return (int) Math.min(pos - min, Integer.MAX_VALUE);
        }
    }

    /**
     * ちょっと分割したいときのBlock.
     */
    static class SubReadableBlock extends AbstractSubReadableBlock {

        private ReadableBlock pa;

        /**
         * 部分集合.
         *
         * @param min 最小位置
         * @param max 最大位置
         * @param p parent block
         */
        SubReadableBlock(long min, long max, ReadableBlock p) {
            super(min, max);
            pa = p;
        }

        @Override
        public ReadableBlock readBlock(int length) {
            int p = pa.backSize();
            pa.seek(pos);
            length = (int) Math.min(max - pos, length);
            ReadableBlock rb = pa.readBlock(length);
            pos = pa.backSize();
            pa.seek(p);
            return rb;
        }

        @Override
        public int read(byte[] d, int offset, int length) {
            int pp = pa.backSize();
            pa.seek(pos);
            length = Math.min(d.length - offset, length);
            length = (int) Math.min(max - pos, length);
            int s = pa.read(d, offset, length);
            pa.seek(pp);
            if (s > 0) {
                pos += s;
            }
            return s;
        }

        @Override
        public int backRead(byte[] data, int offset, int length) {
            int pp = pa.backSize();
            pa.seek(pos);
            length = Math.min(data.length - offset, length);
            length = (int) Math.min(pos - min, length);
            int s = pa.backRead(data, offset, length);
            pa.seek(pp);
            if (s > 0) {
                pos -= s;
            }
            return s;
        }

        @Override
        public boolean isOpen() {
            return pa != null;
        }

        @Override
        public void close() {
            pa = null;
        }
    }
}
