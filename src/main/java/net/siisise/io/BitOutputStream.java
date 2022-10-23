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
 * ビット単位で書き込む。
 * データの未定義部分は 1 (true) である。
 * reset 等は対応していない
 */
public class BitOutputStream extends FilterOutputStream {
    /**
     * 1パイト中何ビット書き込んだか。
     */
    int topbit;
    int buff;

    public BitOutputStream(OutputStream out) {
        super(out);
        topbit = 0;
    }
    
    /**
     * バイト境界にあわせてから書き込む
     * @throws java.io.IOException Filter
     */
    @Override
    public void write(int b) throws java.io.IOException {
        // バイトとビットが独立している場合
        flushBit();
        super.write(b);
        // バイトもビット境界で書き込む場合
        // writeBit(b, 8);
    }
    
    /**
     * 指定ビット書き込み
     * dataの下位ビットから指定ビットを追加する。
     * 1～32ビット 指定可の予定
     * @param data データ
     * @param bit 有効ビット
     * @throws java.io.IOException
     */
    public void writeBit(int data, int bit) throws java.io.IOException {
        // 格納可能ビット
        int clip;
        // 残りビット数計算
        // 0～7になる
        clip = 8 - topbit;
        while (clip <= bit) { // 1バイト書き出し可能 data は右シフト
            buff <<= clip;
            
            // データの未確定上位ビットを排除
            data = data & ((bit < 32) ? ((1 << bit) - 1) : -1);
            
            buff |= data >>> (bit - clip); // 混合
            bit -= clip;
            topbit = 0;
            clip = 8; // 8 - topbit;
            super.write(buff);
            //buff = 0;
        }
        // 端数ビットの保持
        buff <<= bit; // 頭にはゴミが残る
        data = data & ((1 << bit) - 1);
        buff |= data;
        topbit += bit;
    }
    
    /**
     * 下から?
     * 
     * @param data
     * @param bit
     * @throws java.io.IOException
     */
    public void writeBit(long data, int bit) throws java.io.IOException {
        if (bit > 32) {
            writeBit((int)(data >> 32), bit - 32);
            bit = 32;
        }
        writeBit((int)data, bit);
    }
    
    /**
     * 指定ビットの書き込み
     * シフトしながら書き込むため低速?
     * @param data
     * @param offset
     * @param len
     * @throws java.io.IOException
     */
    public void writeBit(byte[] data, int offset, int len) throws java.io.IOException {
//        byte[] buff;
        // 1 ～ 8
        int bitlen = 8 - ( offset % 8 );
        offset = offset / 8;
        
        // 長さチェック
        // する?
        
        if ( bitlen < 8 && bitlen <= len ) {
            writeBit( data[offset], bitlen );
            len -= bitlen;
            offset++;
            bitlen = 8;
        }
        
        while ( 8 <= len ) {
            writeBit(data[offset], 8 );
            len -= 8;
            offset++;
        }
        
        if (len > 0) { // なんかへん?
            int d = data[offset] >> ( bitlen - len );
            writeBit(d, len);
        }
        
        // buff = new byte[len -1];
    }
    
    /**
     * 残ビット処理.
     * @throws java.io.IOException
     */
    public void flushBit() throws java.io.IOException {
        if (topbit > 0) {
            writeBit(buff << (8 -  topbit), 8);
        }
        out.flush();
    }
    
    /**
     *
     * @throws IOException
     */
    @Override
    public void flush() throws java.io.IOException {
        flushBit();
        super.flush();
    }

}
