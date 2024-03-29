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
 * 逆書き.
 * ヘッダを付け足したり(Packet)読み機したところを戻って書いたり(Block)したいことがあるかもしれず
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
     * OverBlock等で先頭を超えるとエラーの場合あり
     * 読んだデータを戻すのに便利.
     * @param src データを含む配列.
     */
    void backWrite(byte[] src);
    
    /**
     * 上限配慮あり.
     * 転送元、転送先どちらかの上限まで移動する.
     * @param rin 逆入力.
     * @return 戻ったサイズ
     */
    long backWrite(RevInput rin);

    /**
     * 逆書き込み
     * @param rin 逆入力
     * @param length 長さ
     * @return 戻ったサイズ
     */
    long backWrite(RevInput rin, long length);

    /**
     * 複製しない(可能な場合)
     * @param src データ列
     */
    void dbackWrite(byte[] src);

    void flush();

    /**
     * どっちから詰める?
     * @param out 出力先
     * @param in 逆入力
     * @param length 長さ
     * @return 移動した長さ
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
