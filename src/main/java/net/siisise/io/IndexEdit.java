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
 * サイズ変更が可能な編集.
 */
public interface IndexEdit extends IndexInput,IndexOutput {
    
    byte del(long index);
    void del(long index, long size);
    <T extends IndexEdit> T del(long index, byte[] buf);
    <T extends IndexEdit> T del(long index, byte[] buf, int offset, int length);

    /**
     * 1バイト追加.
     * @param index 位置
     * @param src 追加するデータ
     */
    void add(long index, byte src);
    /**
     * バイト列を追加.
     * @param index 位置
     * @param src 追加するデータ
     */
    void add(long index, byte[] src);
    /**
     * バイト列の一部を追加.
     * @param index 位置
     * @param src 追加するデータ
     * @param offset データの位置
     * @param length データのサイズ
     */
    void add(long index, byte[] src, int offset, int length);
}
