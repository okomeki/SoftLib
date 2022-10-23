/*
 * Copyright 2006-2022 Siisise Net.
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

import java.io.*;

/**
 * ビット単位の入出力をする。
 * 標準の#read()はビット列を無視する.
 * 要テスト.
 * 
 * @version 0.3
 */
public class BitInputStream extends FilterInputStream {
    /** 読み残しビット数 */
    int topbit;
    /** 読み残しビット */
    int buff;

    public BitInputStream(InputStream in) {
        super(in);
        topbit = 0;
        buff = 0;
    }

    /**
     * ビット処理をリセットしてしまいます
     * 読み込んでいない8ビット未満のビットデータは破棄されます。
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException {
        // 独立している場合
        topbit = 0;
        buff = 0;
        return super.read();
    }
    
    /**
     * ビット処理を考慮せずデータを読み込みます。
     * 読み込んでいない8ビット未満のビットデータは破棄されます。
     * 
     * 2007-12-03 忘れていたらしいので追加
     * 
     * @param data データを受け取る配列
     * @param off オフセット(バイト単位)
     * @param length 受け取る長さ(バイト単位)
     * @return
     * @throws java.io.IOException
     */
    @Override
    public int read( byte[] data, int off, int length ) throws IOException {
        topbit = 0;
        buff = 0;
        return super.read(data, off, length);
    }

    /**
     * ビット単位で読む
     * 0～32ビットに対応
     * EOF 未対応
     * @param len
     * @return 
     * @throws java.io.IOException 
     */
    public int readBit(int len) throws IOException {
        int r = 0;

        while (topbit < len) {
            r <<= topbit;
            r |= buff;
            len -= topbit;
            topbit = 0;

            buff = in.read();
            if (buff == -1) {
                // 未対応
                throw new java.io.EOFException("未実装なので");
            }
            topbit += 8;
        }

        topbit -= len;
        r <<= len;
        r |= buff >>> topbit;
        buff &= (1 << topbit) - 1;

        return r;
    }

    /**
     * 指定ビットを配列に格納する 
     * offset,bitlen はビット単位
     * とりあえず 0で考える
     * dtの格納前のデータは破壊されるので要修正?
     * @param dt
     * @param bitoffset
     * @param bitlen
     * @return 
     * @throws java.io.IOException
     */
    public int readBit(byte[] dt, int bitoffset, int bitlen) throws IOException {
        int retlen;
        int readOffset = bitoffset / 8;

        bitoffset = bitoffset % 8;
        if (bitoffset == 0 && topbit == 0 && bitlen >= 8) { // offset が 0の場合最適化
            retlen = in.read(dt, readOffset, bitlen / 8) * 8;
            if (retlen < 0) {
                return -1;
//                throw new java.io.EOFException("どうする");
            }
            readOffset += retlen / 8;
            bitlen -= retlen;
        } else { // 通常処理だったり
            retlen = 8 - bitoffset;
            if (bitlen >= retlen) {
                // 要ビットマスク
                dt[readOffset++] = (byte) readBit(retlen);
                bitlen -= retlen;
                bitoffset = 0;
            }
        }
        // 1バイトずつ読んでみる処理
        while (bitlen >= 8) {
            dt[readOffset++] = (byte) readBit(8);
            bitlen -= 8;
            retlen += 8;
        }

        // 残りビットの読み取りとシフト
        if (bitlen > 0) {
            dt[readOffset] = (byte) readBit(bitlen);
            dt[readOffset] <<= 8 - bitlen - bitoffset;
            retlen += bitlen;
        }

        return retlen;
    }

    public int readBit(byte[] data) throws IOException {
        return readBit(data, 0, data.length * 8);
    }

    /**
     * バイト単位でデータのある位置のうしろまでシーク
     * 未完成
     * @param pattern
     * @throws java.io.IOException
     */
    public void find(byte[] pattern) throws IOException {
        int read;
        int offset = 0;
        while (offset < pattern.length) {
            do { // 先頭バイト検索
                read = in.read();
            } while ((byte) read != pattern[0]);

            read = in.read();
            if ((byte) read == pattern[offset]) {
                offset++;
            }
        }
    }
}
