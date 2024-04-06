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

import java.io.*;

/**
 * 固定サイズ配列へのStream.
 * Block系でもできそう.
 * Created on 2007/03/11, 1:22
 *
 */
public class ArrayOutputStream extends OutputStream {

    final byte[] buff;
    int offset;

    /**
     * 長さを決めて配列をストリームの受け口にする。
     * @param size 配列長 
     */
    public ArrayOutputStream(int size) {
        buff = new byte[size];
        offset = 0;
    }

    /**
     * 配列を用意してストリーム出力先にする。
     * @param data 転送先配列 
     */
    public ArrayOutputStream(byte[] data) {
        buff = data;
        offset = 0;
    }

    /**
     * 配列の特定位置からストリーム出力先にする。
     * @param data
     * @param offset 
     */
    public ArrayOutputStream(byte[] data, int offset) {
        buff = data;
        this.offset = offset;
    }

    @Override
    public void write(int d) throws IOException {
        try {
            buff[offset++] = (byte) d;
        } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
        }
    }

    /**
     * 
     * @param data 書き着込むデータ
     * @param offset データの先頭位置
     * @param length データの長さ
     * @throws IOException 配列サイズを超えたとき 
     */
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        try {
            System.arraycopy(data, offset, buff, this.offset, length);
            this.offset += length;
        } catch (IndexOutOfBoundsException e) {
            throw new IOException(e);
        }
    }

    /**
     * 複製しない コンストラクタで指定した内部バッファを渡す
     * @return 内部配列
     */
    public byte[] toByteArray() {
        return buff;
    }

}
