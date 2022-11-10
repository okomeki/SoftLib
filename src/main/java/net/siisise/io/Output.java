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

    void dwrite(byte[] data);
    /**
     * 中身の移動.
     * @param pac null不可.
     */
    void write(Input pac);

    /**
     * Block では上書き. 上限あり?
     * Packet 追加. writeと同じ.
     * 
     * @param data データ
     * @return 
     */
    Output put(byte data);
    Output put(byte[] data);
    /**
     * Block では上書き. 上限あり?
     * Packet 追加. writeと同じ.
     * 
     * @param data データ
     * @param offset
     * @param length
     * @return 
     */
    Output put(byte[] data, int offset, int length);

    /**
     * データを小分けにしながら移動。
     * 同じ実装が多そうなのでまとめる。
     * @param out 先 null不可
     * @param in 元 null不可
     * @param length データサイズ
     * @return 移動したサイズ
     */
    public static long write(Output out, Input in, long length) {
        if ( in instanceof PacketA && out instanceof PacketA ) {
            return ((PacketA)out).write(in, length);
        }
        byte[] d;
        int size;
        long x = length;
        while (x > 0) {
            d = new byte[(int)Math.min(x,PacketA.MAXLENGTH)];
            size = in.read(d);
            if ( size <= 0) {
                return length - x;
            }
            if ( size == d.length) {
                out.dwrite(d);
            } else {
                out.write(d,0,size);
            }
            x -= size;
        }
        return length - x;
    }
}
