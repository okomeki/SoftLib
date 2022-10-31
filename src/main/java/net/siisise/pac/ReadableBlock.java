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
import java.nio.charset.StandardCharsets;
import net.siisise.io.FrontPacket;
import net.siisise.io.Input;
import net.siisise.io.RevInput;

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

    /**
     * 使いやすそうなのでラップする.
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
     * ちょっと分割したいときのBlock.
     */
    static class SubReadableBlock extends AbstractSubReadableBlock {

        private final ReadableBlock pa;

        /**
         *  部分集合.
         * @param min 最小位置
         * @param max 最大位置
         * @param p parent block
         */
        SubReadableBlock(int min, int max, ReadableBlock p) {
            super(min,max);
            pa = p;
        }

        @Override
        public ReadableBlock readBlock(int length) {
            int p = pa.backSize();
            pa.seek(pos);
            length = Integer.min(max - pos, length);
            ReadableBlock rb = pa.readBlock(length);
            pos = pa.backSize();
            pa.seek(p);
            return rb;
        }

        @Override
        public int read(byte[] d, int offset, int length) {
            int pp = pa.backSize();
            pa.seek(pos);
            length = Integer.min(d.length - offset, length);
            length = Integer.min(max - pos, length);
            int s = pa.read(d, offset, length);
            pa.seek(pp);
            if ( s > 0 ) {
                pos += s;
            }
            return s;
        }

        @Override
        public int backRead(byte[] data, int offset, int length) {
            int pp = pa.backSize();
            pa.seek(pos);
            length = Integer.min(data.length - offset, length);
            length = Integer.min(pos - min, length);
            int s = pa.backRead(data,offset,length);
            pa.seek(pp);
            if ( s > 0 ) {
                pos -= s;
            }
            return s;
        }
    }
}
