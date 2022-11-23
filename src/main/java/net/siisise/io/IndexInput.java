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

/**
 * Packet, Blockの指定位置を読む系
 */
public interface IndexInput {

    /**
     * 読めないときはException
     * @param index
     * @return データ
     */
    byte get(long index);

    /**
     * 読めないときはException
     * @param index
     * @param b
     * @return これ
     */
    IndexInput get(long index, byte[] b);

    /**
     * 読めないときはException
     * Buffer 系にあわせたが、Channel系にあわせる方がいいのかもしれない.
     * Packetを読んでもとりあえず消えないことにする.
     * @param index
     * @param b
     * @param offset
     * @param length
     * @return これ
     */
    IndexInput get(long index, byte[] b, int offset, int length);
    

}
