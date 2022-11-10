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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import net.siisise.io.BackPacket;
import net.siisise.io.Base;
import net.siisise.io.FrontPacket;
import net.siisise.io.IndexInput;
import net.siisise.io.IndexOutput;
import net.siisise.io.Output;
import net.siisise.math.Matics;

/**
 * 上書きのみできるブロック.
 * 切り取ったブロックなどでサイズ変更ができないが上書きは可能。
 */
public interface OverBlock extends ReadableBlock, FrontPacket, BackPacket, IndexInput, IndexOutput {
    
    @Override
    OverBlock flip();

    public static OverBlock wrap(byte[] b) {
        return new ByteBlock(b);
    }

    public static OverBlock wrap(ByteBuffer bb) {
        if ( bb.hasArray() ) {
            return wrap(bb.array(),bb.arrayOffset(),bb.limit());
        }
        return new ByteBufferBlock(bb);
    }

    public static OverBlock wrap(byte[] b, int offset, int length) {
        return new ByteBlock(b, offset, length);
    }
    
    public static OverBlock wrap(OverBlock block, long offset, long length) {
        return new SubOverBlock(offset, offset + length, block);
    }

    public static OverBlock wrap(FileChannel ch) throws IOException {
        MappedByteBuffer bb = ch.map(FileChannel.MapMode.READ_WRITE, 0, ch.size());
        return new ByteBufferBlock(bb);
    }

    
    /**
    * 指定サイズの部分集合を作る.
    * offsetは読み込んだ分進む.
    * @param length サイズ
    * @return 部分集合 subblock
    */
    @Override
    default OverBlock readBlock(long length) {
        length = Matics.range(length, 0, length());
        OverBlock sb = sub(backLength(), length);
        skip(length);
        return sb;
    }

    @Override
    default OverBlock sub(long index, long length) {
        if ( !Matics.sorted(0, index, index + length, backLength() + length())) {
            throw new java.nio.BufferOverflowException();
        }
        return new SubOverBlock(index, index + length, this);
    }
    
    /**
     * 上書き可能なブロック、サブブロック.
     * 領域以外の実装。
     * ToDo: 実実装判定多め.
     */
    public static abstract class AbstractSubOverBlock extends Base implements OverBlock {

        final long min;
        final long max;
        long pos;

        AbstractSubOverBlock(long min, long max) {
            if ( !Matics.sorted(0,min,max) ) {
                throw new java.lang.IllegalStateException();
            }
            this.min = pos = min;
            this.max = max;
        }
        
        @Override
        public InputStream getInputStream() {
            return new ReadableBlock.BlockInput(this);
        }

        /**
         * 範囲内で移動する.
         * ReadableBlockと同じ.
         * @param position 絶対位置
         * @return 位置.
         */
        @Override
        public long seek(long position) {
            pos = Matics.range(position + min, min, max);
            return pos - min;
        }

        /**
         * 読み書きせずに進む.
         * @param length 進む相対位置
         * @return 進んだサイズ
         */
        @Override
        public long skip(long length) {
            long op = pos;
            pos = Matics.range(pos + length, min, max);
            return pos - op;
        }

        /**
         * 読み書きせずに戻る.
         * @param length 戻る相対位置
         * @return 戻ったサイズ
         */
        @Override
        public long back(long length) {
            long op = pos;
            pos = Matics.range(pos - length, min, max);
            return op - pos;
        }

        /**
         * 読めるサイズlong版.
         * @return size 読めるサイズ
         */
        @Override
        public long length() {
            return max - pos;
        }

        /**
         * 読めるbackサイズlong版.
         * @return back size back系で読めるサイズ
         */
        @Override
        public long backLength() {
            return pos - min;
        }

        /**
         * 戻り書く.
         * @param data データ
         * @param offset data内のデータの開始位置
         * @param length データサイズ
         */
        @Override
        public void backWrite(byte[] data, int offset, int length) {
            if ( !Matics.sorted(0,length, backSize())) {
                throw new java.nio.BufferOverflowException();
            }
            put(backSize() - length, data, offset, length);
        }

        /**
         * 書く.
         * @param data データ
         * @param offset 位置
         * @param length 長さ
         * @return これ
         */
        @Override
        public Output put(byte[] data, int offset, int length) {
            if ( !Matics.sorted(0, length, length())) {
                throw new java.nio.BufferOverflowException();
            }
            write(data, offset, length);
            return this;
        }

        /**
         * 書き込む.
         * @param src データ
         * @return 書いたサイズ
         */
        @Override
        public int write(ByteBuffer src) {
            int len = Math.min(src.remaining(), size());
            if ( src.hasArray() ) {
                int p = src.position();
                write(src.array(), src.arrayOffset() + p, len);
                src.position(p + len);
            } else {
                byte[] d = new byte[len];
                src.get(d);
                write(d,0,len);
            }
            return len;
        }

        /**
         * 切り取りはできない
         *
         * @return
         */
        @Override
        public OverBlock flip() {
            return sub(0,backSize());
        }
    }

    /**
     * 切り取られた空間.
     */
    public static class SubOverBlock extends AbstractSubOverBlock {

        private final OverBlock block;
        
        public SubOverBlock(long min, long max, OverBlock block) {
            super(min,max);
            if ( !Matics.sorted(0,min,max,block.backLength() + block.length())) {
                throw new java.lang.IllegalStateException();
            }
            this.block = block;
        }

        @Override
        public int read(byte[] d, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, d.length)) {
                throw new java.nio.BufferOverflowException();
            }
            long p = block.backLength();
            block.seek(pos);
//            if ( !Matics.sorted(0, length, size()) ) {
//                throw new java.nio.BufferOverflowException();
//            }
            length = Math.min(length, size());
            int s = block.read(d,offset,length);
            pos += s;
            block.seek(p);
            return s;
        }

        @Override
        public IndexInput get(long index, byte[] b, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, b.length) || index + length > max - min ) {
                throw new java.nio.BufferOverflowException();
            }
            block.get(min + index,b,offset,length);
            return this;
        }

        /**
         * 逆から読む.
         * @param dst
         * @param offset
         * @param length
         * @return 
         */
        @Override
        public int backRead(byte[] dst, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, dst.length)) {
                throw new java.nio.BufferOverflowException();
            }
            long p = block.backLength();
            block.seek(pos);
//            if ( !Matics.sorted(0, length, size()) ) {
//                throw new java.nio.BufferOverflowException();
//            }
            length = Math.min(length, backSize());
            int s = block.backRead(dst, offset, length);
            pos -= s;
            block.seek(p);
            return s;
        }

        /**
         * 上限ぎりぎりまで書き込む.
         * @param data データ
         * @param offset 位置
         * @param length サイズ
         */
        @Override
        public void write(byte[] data, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, data.length)) {
                throw new java.nio.BufferOverflowException();
            }
            length = Math.min(length, size());
            long p = block.backLength();
            block.seek(pos);
            block.write(data,offset,length);
            pos += length;
            block.seek(p);
        }

        @Override
        public void put(long index, byte[] d, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, d.length) || index + length > max - min ) {
                throw new java.nio.BufferOverflowException();
            }
            block.put(min + index, d, offset, length);
        }

        /**
         * 小分けにする.
         * @param length
         * @return 
         */
        @Override
        public OverBlock readBlock(long length) {
            length = Matics.range(length, 0, length());
            OverBlock b = sub(backLength(),length);
            skip(length);
            return b;
        }

        @Override
        public OverBlock sub(long index, long length) {
            if ( !Matics.sorted(0, index, index + length, max - min) ) {
                throw new java.nio.BufferOverflowException();
            }
            return OverBlock.wrap(block, min + index, length);
        }
    }
}
