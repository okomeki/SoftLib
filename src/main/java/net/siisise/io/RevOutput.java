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
 *
 */
public interface RevOutput {

    /**
     * 書き戻す用.
     * Packetの頭にデータが増える.
     * 頭に書き足す/戻すので繋ぎ方に注意.
     * @return 特殊用途OutputStream
     */
    OutputStream getBackOutputStream();

    /**
     * 読み出しの手前に1バイト戻す.
     * @param data 下位8ビットのみ
     */
    void backWrite(int data);

    /**
     * 読み出しの手前にデータを足す.
     * 読んだデータを戻すのに便利.
     * @param data データを含む配列.
     * @param offset 開始位置
     * @param length データ長
     */
    void backWrite(byte[] data, int offset, int length);

    /**
     * 読み出しの手前にデータを足す.
     * 読んだデータを戻すのに便利.
     * @param data データを含む配列.
     */
    void backWrite(byte[] data);

    /**
     * 複製しない(可能な場合)
     * @param data データ列
     */
    void dbackWrite(byte[] data);

    void flush();

    /**
     * どっちから詰める?
     * @param out
     * @param in
     * @param length
     * @return 
     */
    public static long backWrite(RevOutput out, RevInput in, long length) {
        byte[] d;
        int size;
        long x = length;
        while (x > 0) {
            d = new byte[(int)Math.min(x,PacketA.MAXLENGTH)];
            size = in.backRead(d);
            if ( size <= 0) {
                return length - x;
            }
            if ( size == d.length) {
                out.dbackWrite(d);
            } else {
                out.backWrite(d,d.length - size,size);
            }
            x -= size;
        }
        return length - x;
    }
}
