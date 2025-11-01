/*
 * Copyright 2025 okome.
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
 * BitPacket から入力系の分割.
 *
 */
public interface BitInput {

    /**
     * ビット読み込み.
     * 下位ビットを利用
     *
     * @param bitLength 読み込むビットサイズ 0 - 32
     * @return 下位ビットに読み込み
     */
    int readInt(int bitLength);

    /**
     * ビット単位で読み込み。
     * BigBitPacketではビット単位ではMSBから埋める.
     * LittleBitPacketではLSBから埋める.
     *
     * @param data 読み込み先
     * @param offsetBit data列の先頭ビット位置
     * @param bitLength 読み込むビット長
     * @return 読み込んだビット長
     */
    long readBit(byte[] data, long offsetBit, long bitLength);

    /**
     * 読み込み可能なビット数.
     * @return 端数含むビット数
     */
    long bitLength();
}
