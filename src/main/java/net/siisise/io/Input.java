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
package net.siisise.io;

import java.io.InputStream;
import java.nio.ByteBuffer;
import net.siisise.block.ByteBlock;
import net.siisise.block.OverBlock;

/**
 * InputStream の Interface化 + ちょっと
 *
 */
public interface Input {

    /**
     * ストリームと完全互換ではないがそれっぽくしてくれる.
     *
     * @return InputStream への変換
     */
    InputStream getInputStream();

    /**
     * InputStreamとの違い
     * 入力ブロックせずに
     * データ:0-255 または データ無し:-1 を返す
     *
     * @return -1 または 0-255
     */
    int read();

    /**
     * 配列の一部に読む.
     * ないときは サイズ0.
     * length と length() の小さい範囲
     *
     * @param buf バッファ
     * @param offset バッファ位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    int read(byte[] buf, int offset, int length);

    /**
     * 配列に読む.
     * ないときは サイズ0
     *
     * read(d,0,d.length) と同じ
     *
     * @param d バッファ
     * @return 読めたサイズ
     * @see #read(byte[],int,int)
     */
    int read(byte[] d);

    /**
     * データ移動的なところ (Packet / Block 汎用)
     * readでもwritwでも同じ
     *
     * @param dst 転送先
     * @return これとdstの size() 短い方
     */
    default long read(Output dst) {
        if (dst instanceof OverBlock && ((OverBlock) dst).hasArray()) {
            OverBlock o = (OverBlock) dst;
            long size = read(o.array(), o.arrayOffset() + o.backSize(), o.size());
            o.skip(size);
            return size;
        }
        return dst.write(this);
    }

    byte get();

    default long get(byte[] b) {
        return get(b, 0, b.length);
    }

    long get(byte[] b, int offset, int length);

    default long get(Output out) {
        if (out instanceof OverBlock && ((OverBlock) out).hasArray()) {
            ByteBlock o = (ByteBlock) out;
            long size = read(o.array(), o.arrayOffset() + o.backSize(), o.size());
            o.skip(size);
            return size;
        }
        return out.write(this);
    }

    default int read(ByteBuffer dst) {
        int len = Math.min(dst.remaining(), size());
        if (dst.hasArray()) {
            int p = dst.position();
            int size = read(dst.array(), dst.arrayOffset() + p, len);
            dst.position(p + size);
            return size;
        } else {
            byte[] d = new byte[len];
            int size = read(d);
            dst.put(d, 0, size);
            return size;
        }
    }

    /**
     * byte配列に変換する。
     *
     * @return readするデータの配列
     */
    byte[] toByteArray();

    /**
     * 内部的に分割を高速にしたい処理。
     * position的なところから length 分をPacket にreadで読ませる程度の処理
     *
     * @param length 長さ
     * @return 分割したPacket
     */
    Packet readPacket(long length);

    /**
     * InputStream とあわせる.
     *
     * @param length skipするサイズ
     * @return skipしたサイズ
     */
    long skip(long length);

    /**
     * 読めるサイズ long版.
     * 32ビットでは足りないかもと足してみた
     *
     * @return サイズ
     */
    long length();

    /**
     * 読めるサイズ int版.
     * 32ビット内であればそのサイズ、それ以上はIntegerの最大値.
     * StreamのFrontの場合は信用しない方がいい
     *
     * @return サイズ
     */
    int size();

    /**
     * 標準的なパケットを返す場合の実装.
     *
     * @param in ストリーム
     * @param length 長さ
     * @return 分けたデータ
     */
    public static Packet splitImpl(Input in, long length) {
        PacketA pac = new PacketA();
        pac.write(in, length);
        return pac;
    }

    /**
     * 物理的に読んで捨てるときに使う.
     *
     * @param in ストリーム Packet など
     * @param length サイズ
     * @return 移動したサイズ
     */
    public static long skipImpl(Input in, long length) {
        long r = length;
        byte[] t = new byte[(int) Math.min(length, PacketA.MAXLENGTH)];
        while (r > 0 && in.length() > 0) {
            int s = in.read(t, 0, (int) Math.min(r, t.length));
            if (s <= 0) {
                break;
            }
            r -= s;
        }
        return length - r;
    }

    /**
     * Abstract的な
     */
    public abstract static class AbstractInput extends InputStream implements Input {

        @Override
        public byte get() {
            byte[] b = new byte[1];
            get(b, 0, 1);
            return (byte) b[0];
        }

        @Override
        public long get(byte[] b, int offset, int length) {
            int len = size();
            if (len < length) {
                throw new java.nio.BufferUnderflowException();
            }
            int s = read(b, offset, length);
            if (s < 0) {
                throw new java.nio.BufferUnderflowException();
            }
            return length;
        }

        @Override
        public int read() {
            byte[] d = new byte[1];
            int s = read(d);
            return s < 1 ? -1 : d[0] & 0xff;
        }

        @Override
        public InputStream getInputStream() {
            return this;
        }

        @Override
        public int read(byte[] d) {
            return read(d, 0, d.length);
        }

        /**
         * ないときは サイズ0
         *
         * @param buf バッファ
         * @param offset バッファ位置
         * @param length サイズ
         * @return 読めたサイズ
         */
        @Override
        public abstract int read(byte[] buf, int offset, int length);

        /**
         * バイト列にする.
         *
         * @return available() な中身
         */
        @Override
        public byte[] toByteArray() {
            byte[] b = new byte[size()];
            read(b);
            return b;
        }

        /**
         * InputStream 用サイズ.
         *
         * @return size
         */
        @Override
        public int available() {
            return size();
        }

        /**
         * 読まずに進む.
         *
         * @param length 相対サイズ
         * @return 移動したサイズ
         */
        @Override
        public long skip(long length) {
            return Input.skipImpl(this, length);
        }

        /**
         * PacketAを使った簡易実装.
         *
         * @param length
         * @return
         */
        @Override
        public Packet readPacket(long length) {
            PacketA pac = new PacketA();
            pac.write(this, length);
            return pac;
        }

        @Override
        public int size() {
            return (int) Math.min(length(), Integer.MAX_VALUE);
        }
    }

}
