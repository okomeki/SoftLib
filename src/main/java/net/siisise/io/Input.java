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

/**
 * InputStream の Interface化 + ちょっと
 *
 */
public interface Input {

    /**
     * ストリームと完全互換ではないがそれっぽくしてくれる.
     *
     * @return
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
     * ないときは サイズ0
     *
     * @param d
     * @param offset
     * @param length
     * @return
     */
    int read(byte[] d, int offset, int length);
    int read(byte[] d);
/*
// FrontInput
    byte get();
    Input get(byte[] d);
    Input get(byte[] d, int offset, int length);
*/
    /**
     * byte配列に変換する。
     * @return 全データの配列
     */
    byte[] toByteArray();

    /**
     * 内部的に分割を高速にしたい処理。
     * readPacket 的なもの
     * 試験的導入.
     *
     * @param length 長さ
     * @return 分割したPacket
     */
    Packet split(long length);
    /**
     * InputStream とあわせる.
     * @param length
     * @return 
     */
    long skip(long length);
    
    /**
     * 読めるサイズ long版.
     * 32ビットでは足りないかもと足してみた
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
     * @param in
     * @param length
     * @return 
     */
    public static Packet splitImpl(Input in, long length) {
        PacketA pac = new PacketA();
        pac.write(in, length);
        return pac;
    }
    
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
}
