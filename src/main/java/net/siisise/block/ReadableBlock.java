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
import net.siisise.io.FrontInput;
import net.siisise.io.FrontPacket;
import net.siisise.io.IndexInput;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.io.ReadBase;
import net.siisise.io.RevInput;
import net.siisise.io.RevOutput;
import net.siisise.io.StreamFrontPacket;
import net.siisise.math.Matics;

/**
 * Buffer の読み込み専用 っぽいものをStream風メソッドで実装したもの.
 * position() は backSize()
 */
public interface ReadableBlock extends Block, FrontInput, RevInput, IndexInput {

    /**
     * 読み込み専用のpositionまでの切り取り.
     * @return 読み専用
     */
    @Override
    ReadableBlock flip();

    /**
     * 部分集合を作る。
     * メモリを共有した状態で範囲を制限して分ける
     * @param index 位置
     * @param length 長さ
     * @return 戻り型はReadableBlock
     */
    @Override
    ReadableBlock sub(long index, long length);
  
// 実装が上の方(Base)にあると型が解決できないのとgetの戻り型は重要ではないので略
//    @Override
//    ReadableBlock get(long index, byte[] b);
    @Override
    ReadableBlock get(long index, byte[] b, int offset, int length);
    
    /**
     * 現在値から部分的な切り出し.
     * メモリ空間は可能な場合共有する.
     *
     * @param length 長さ
     * @return 部分要素.
     */
    ReadableBlock readBlock(long length);

    public static ReadableBlock wrap(ReadableBlock rb,long offset, long length) {
        return new SubReadableBlock(offset,Math.min(offset + length,rb.backLength() + rb.length()), rb);
    }

    public static ReadableBlock wrap(String s) {
        return new ByteBlock(s.getBytes(StandardCharsets.UTF_8));
    }

    public static ReadableBlock wrap(byte[] b) {
        return new ByteBlock(b);
    }

    /**
     * バイト列を元にBlockを作成.
     * メモリ空間は共有する.
     * @param b バイト列
     * @param offset 位置
     * @param length サイズ
     * @return 読み専用のつもりのByteBlock
     */
    public static ReadableBlock wrap(byte[] b, int offset, int length) {
        return new ByteBlock(b, offset, length);
    }

    public static ReadableBlock wrap(File file) throws FileNotFoundException {
        return ChannelBlock.wrap(file);
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

    /**
     * position より後はpacに収まっているといい
     * @param pac 元
     * @return ReadableBlockをかぶせたもの
     */
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

    public static ReadableBlock wrap(Input in, long length) {
        OverBlock b = OverBlock.wrap(new byte[(int)length]);
        b.write(in);
        return b;
    }
    
    static class BlockInput extends FilterInput {
        int mark = -1;
        ReadableBlock in;
        
        BlockInput(ReadableBlock in) {
            super(in);
            this.in = in;
        }
        
        @Override
        public boolean markSupported() {
            return true;
        }
        
        @Override
        public void mark(int readlimit) {
            mark = in.backSize();
        }
        
        @Override
        public void reset() {
            if ( mark >= 0) {
                in.seek(mark);
            }
        }
        
    }

    /**
     * 一般的なところだけ載せる.
     */
    public abstract class AbstractReadableBlock extends ReadBase implements ReadableBlock, Iterator<Byte> {

        @Override
        public InputStream getInputStream() {
            return new BlockInput(this);
        }

        @Override
        public long seek(long position) {
            position = Matics.range(0, position, backLength() + length());
            long size = position - backSize();
            if (size > 0) {
                skip(size);
            } else {
                back(-size);
            }
            return position;
        }
        
        @Override
        public ReadableBlock readBlock(long length) {
            length = Math.min(length, length());
            ReadableBlock sb = sub(backLength(), length);
            skip(length);
            return sb;
        }

        @Override
        public ReadableBlock sub(long index, long length) {
            length = Matics.range(length, 0, length());
            return new SubReadableBlock(index, index + length, this);
        }

        @Override
        public ReadableBlock get(long index, byte[] d, int offset, int length) {
            long p = backLength();
            seek(index);
            get(d, offset, length);
            seek(p);
            return this;
        }
        
        public IndexInput get(long index, OverBlock bb) {
            long p = backLength();
            seek(index);
            get(bb);
            seek(p);
            return this;
        }

        /**
         * ここまでを切り出すが、ポインタは移動しない。
         *
         * @return
         */
        @Override
        public ReadableBlock flip() {
            return sub(0,backSize());
        }

        /**
         * 仮
         *
         * @param length
         * @return
         */
        @Override
        public Packet backSplit(long length) {
            length = Math.min(length, backSize());
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
         *
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
            return (byte) v;
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
            if (!Matics.sorted(0, min, max)) {
                throw new java.lang.IllegalStateException();
            }
        }

        /**
         * 範囲内で移動する.
         * Block内の指定の位置に移動する.
         *
         * 切り取った場合もSubBlockの先頭が0
         * @param position 位置
         * @return 位置.
         */
        @Override
        public long seek(long position) {
            pos = Matics.range(min + position, min, max);
            return pos - min;
        }

        /**
         * 後方(読み方向)にpositionを移動する.
         * @param length マイナスの場合は back
         * @return 移動したサイズ
         */
        @Override
        public long skip(long length) {
            if (length < 0) {
                return -back(-Long.max(length, -backLength())); // Long.MIN_VALUE を回避
            }
            long size = Math.min(length(), length);
            pos += size;
            return size;
        }

        /**
         * 先頭方向にpositionを移動する.
         * 超えた場合は先頭に移動する.
         * @param length 移動量 マイナスの場合はskip
         * @return 移動したサイズ
         */
        @Override
        public long back(long length) {
            if (length < 0) {
                return -skip(-Long.max(length, -length())); // Long.MIN_VALUE を回避
            }
            length = Math.min(backLength(), length);
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
        public long backLength() {
            return pos - min;
        }
    }

    /**
     * ちょっと分割したいときのBlock.
     * min, max, pos は parent 基準で設定される。
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
            if ( max > p.backLength() + p.length() ) {
                throw new java.nio.BufferOverflowException();
            }
            pa = p;
        }

        @Override
        public ReadableBlock readBlock(long length) {
            long p = pa.backLength();
            pa.seek(pos);
            length = Matics.range(length, 0, max - pos);
            ReadableBlock rb = pa.readBlock(length);
            pos = pa.backLength();
            pa.seek(p);
            return rb;
        }

        /**
         *  読む
         * @param buf バッファ
         * @param offset バッファ位置
         * @param length サイズ
         * @return 読めた長さ
         */
        @Override
        public int read(byte[] buf, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, buf.length)) {
                throw new java.nio.BufferOverflowException();
            }
            long p = pa.backLength();
            pa.seek(pos);
            length = (int) Math.min(max - pos, length);
            int s = pa.read(buf, offset, length);
            pos = pa.backLength();
            pa.seek(p);
            return s;
        }

        /**
         * 逆から読む.
         * @param d バッファ
         * @param offset バッファ位置
         * @param length 長さ
         * @return 読めた長さ
         */
        @Override
        public int backRead(byte[] d, int offset, int length) {
            if ( !Matics.sorted(0,offset,offset + length, d.length)) {
                throw new java.nio.BufferOverflowException();
            }
            int size = (int) Matics.range(length, 0, pos - min);
            pos -= size;
            pa.get(pos, d, offset + length - size, size);
            return size;
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
