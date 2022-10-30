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

    /**
     * byte配列に変換する。
     * @return 全データの配列
     */
    byte[] toByteArray();

    /**
     * 内部的に分割を高速にしたい処理。
     * 試験的導入.
     *
     * @param length 長さ
     * @return 分割したPacket
     */
    FrontPacket split(int length);
    
    /**
     * 32ビットでは足りないかもと足してみた
     * @return サイズ
     */
    long length();

    /**
     * 32ビット内であればそのサイズ、それ以上はIntegerの最大値.
     * StreamのFrontの場合は信用しない方がいい
     *
     * @return サイズ
     */
    int size();

}
