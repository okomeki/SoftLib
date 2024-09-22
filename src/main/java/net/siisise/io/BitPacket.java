/*
 * Copyright 2019-2022 Siisise Net.
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
 * javaのBitSetと互換かなにかにしたい.
 * ビット系からバイト系に変換する際には端数ビットを捨てる方向で調整する。
 * 必要ならパディングを入れてみよう。
 */
public interface BitPacket extends FrontPacket,BackPacket {

    int readInt(int bitLength);
    int backReadInt(int bitLength);
    /**
     * ビット単位で読み込み。
     * ビット単位ではMSBから埋める.
     * @param data 読み込み先
     * @param offsetBit data列の先頭ビット位置
     * @param bitLength 読み込むビット長
     * @return 読み込んだビット長
     */
    long readBit(byte[] data, long offsetBit, long bitLength);
    long backReadBit(byte[] data, long offsetBit, long bitLength);

    BitPacket readPac(int length);

    void writeBit(int data, int bitLength);
    void backWriteBit(int data, int bitLength);
    void writeBit(byte[] data, long offsetBit, long bitLength);
    void backWriteBit(byte[] data, long offsetBit, long bitLength);

    void writeBit(BitPacket pac, long bitLength);
    void backWriteBit(BitPacket pac, long bitLength);
    void writeBit(BitPacket pac);
    void backWriteBit(BitPacket pac);

    long bitLength();
}
