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

import java.io.OutputStream;
import net.siisise.block.OverBlock;
import net.siisise.block.ReadableBlock;

/**
 * OutputStream風のなにか.
 */
public interface Output {

    OutputStream getOutputStream();

    /**
     * 上限のない書き込み.
     * OutputStream 互換っぽくする。
     * Blockの上限ではなんとかする?
     *
     * @param data データ
     */
    void write(int data);

    /**
     * 上限のない書き込み.
     * OutputStream 互換っぽくする。
     * Blockの上限ではなんとかする?
     *
     * @param data データ
     */
    void write(byte[] data);

    /**
     * 上限のない書き込み.
     * OutputStream 互換っぽくする。
     * Blockの上限ではなんとかする?
     *
     * @param data データ
     * @param offset データ位置
     * @param length データサイズ
     */
    void write(byte[] data, int offset, int length);

    /**
     * 直書き.
     * 主にPacketでデータをコピーしないでそのまま内部配列として利用する.
     * データ列は外側で再利用しないこと.
     *
     * @param data データ列
     */
    void dwrite(byte[] data);

    /**
     * 書き込み.中身の移動.
     * 転送元、転送先どちらかの上限まで移動する.
     * エラーを抑えた動作に振ってみる.
     *
     * @param pac null不可.
     * @return 移動したサイズ
     */
    long write(Input pac);

    /**
     * データ移動.
     * length OverBlock で length 以下の場合はエラー
     *
     * @param pac データ
     * @param length pac data length
     * @return 移動サイズ
     */
    long write(Input pac, long length);

    /**
     * 上書き. 上限あり?
     *
     * @param data 1バイトデータ
     * @return これ
     */
    Output put(byte data);

    /**
     * 上書き. 上限あり?
     *
     * @param data データ
     * @return これ
     */
    Output put(byte[] data);

    /**
     * 上書き.
     *
     * @param data データ
     * @param offset データ位置
     * @param length サイズ
     * @return これ
     */
    Output put(byte[] data, int offset, int length);

    /**
     * データを小分けにしながら移動。
     * 同じ実装が多そうなのでまとめる。
     *
     * @param out 先 null不可
     * @param in 元 null不可
     * @param length データサイズ 調整はここまでで済ませておくこと
     * @return 移動したサイズ
     */
    public static long write(Output out, Input in, long length) {
        if (in instanceof PacketA && out instanceof PacketA) {
            return ((PacketA) out).write(in, length);
        } else if ( out instanceof OverBlock && ((OverBlock)out).hasArray()) {
            OverBlock o = (OverBlock)out;
            int size = in.read(o.array(), o.arrayOffset() + o.backSize(), (int)length);
            o.seek(size);
            return size;
        } else if ( in instanceof ReadableBlock && ((ReadableBlock)in).hasArray()) {
            ReadableBlock i = (ReadableBlock)in;
            int size = (int)Math.min(i.size(), length);
            out.write(i.array(), i.arrayOffset() + i.backSize(), (int)size);
            i.seek(size);
            return size;
        }
        byte[] d;
        int size;
        long x = length;
        while (x > 0) {
            d = new byte[(int) Math.min(x, PacketA.MAXLENGTH)];
            size = in.read(d, 0, d.length);
            if (size <= 0) {
                return length - x;
            }
            if (size == d.length) {
                out.dwrite(d);
            } else {
                out.write(d, 0, size);
            }
            x -= size;
        }
        return length - x;
    }

    public static abstract class AbstractOutput extends OutputStream implements Output {

        @Override
        public OutputStream getOutputStream() {
            return this;
        }

        @Override
        public void write(int d) {
            write(new byte[] {(byte)d});
        }

        @Override
        public void write(byte[] d) {
            write(d,0,d.length);
        }

        @Override
        public void write(byte[] d, int offset, int length) {
            put(d, offset, length);
        };

        @Override
        public void dwrite(byte[] d) {
            write(d);
        }

        /**
         * データの移動.
         * @param src 入力元
         * @return データ移動したサイズ
         */
        @Override
        public long write(Input src) {
            return Output.write(this, src, src.length());
        }

        /**
         * データの移動.
         * @param pac 移動元
         * @param length サイズ
         * @return 移動したサイズ
         */
        @Override
        public long write(Input pac, long length) {
            return Output.write(this, pac, length);
        }

        @Override
        public Output put(byte data) {
            return put(new byte[] {data});
        }

        @Override
        public Output put(byte[] data) {
            return put(data,0,data.length);
        }
    }
}
