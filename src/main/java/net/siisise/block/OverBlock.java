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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import net.siisise.io.BackPacket;
import net.siisise.io.Base;
import net.siisise.io.FrontPacket;
import net.siisise.io.IndexInput;
import net.siisise.io.IndexOutput;
import net.siisise.io.Output;

/**
 * 上書きのみできるブロック.
 */
public interface OverBlock extends ReadableBlock, FrontPacket, BackPacket, IndexInput, IndexOutput {

    public static OverBlock wrap(byte[] b) {
        return new ByteBlock(b);
    }

    public static OverBlock wrap(ByteBuffer bb) {
        return new ByteBufferBlock(bb);
    }

    public static OverBlock wrap(byte[] b, int offset, int length) {
        return new ByteBlock(b, offset, length);
    }

    public static OverBlock wrap(FileChannel ch) throws IOException {
        MappedByteBuffer bb = ch.map(FileChannel.MapMode.READ_WRITE, 0, ch.size());
        return new ByteBufferBlock(bb);
    }

    /**
     * 上書き可能なブロック、サブブロック.
     */
    public static abstract class AbstractSubOverBlock extends Base implements OverBlock {

        final long min;
        final long max;
        long pos;

        AbstractSubOverBlock(long min, long max) {
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
            length = Long.min(length(), length);
            pos += length;
            return length;
        }

        @Override
        public long back(long length) {
            if (length < 0) {
                return -skip(-Long.max(length, -size()));
            }
            length = Long.min(pos - min, length);
            pos -= length;
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

        @Override
        public void backWrite(byte[] data, int offset, int length) {
            if (backSize() < length) {
                throw new java.nio.BufferUnderflowException();
            }
            put(backSize() - length, data, offset, length);
        }

        @Override
        public Output put(byte[] data, int offset, int length) {
            if (length() < length) {
                throw new java.nio.BufferOverflowException();
            }
            write(data, offset, length);
            return this;
        }

        /**
         * 上限あり.
         * @param src
         * @return 
         */
        @Override
        public int write(ByteBuffer src) {
            int r = src.remaining();
            int s = size();
            int m = Math.min(r, s);
            if ( src.hasArray() ) {
                int p = src.position();
                write(src.array(), src.arrayOffset() + p, m);
                src.position(s);
            } else {
                byte[] d = new byte[r];
                src.get(d);
                write(d,0,m);
            }
            return m;
        }

        /**
         * 切り取りはできない
         *
         * @return
         */
        @Override
        public ReadableBlock flip() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class SubOverBlock extends AbstractSubOverBlock {

        private final OverBlock block;
        
        public SubOverBlock(long min, long max, OverBlock block) {
            super(min,max);
            this.block = block;
        }

        @Override
        public int read(byte[] d, int offset, int length) {
            return block.read(d,offset,length);
        }

        @Override
        public IndexInput get(long index, byte[] b, int offset, int length) {
            return block.get(index,b,offset,length);
        }

        @Override
        public int backRead(byte[] data, int offset, int length) {
            return block.backRead(data, offset, length);
        }

        @Override
        public void write(byte[] data, int offset, int length) {
            block.write(data,offset,length);
        }

        @Override
        public void put(long index, byte[] d, int offset, int length) {
            block.put(index, d, offset, length);
        }

        @Override
        public ReadableBlock readBlock(int length) {
            return block.readBlock(length);
        }
    }
}
