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

import java.nio.ByteBuffer;

/**
 * サイズ変更が伴う追加、削除を可能にしたもの.
 */
public abstract class Edit extends Base implements IndexEdit {

    /**
     * 1バイト追加.
     * @param index 位置
     * @param b データ1バイト
     */
    @Override
    public void add(long index, byte b) {
        add(index, new byte[] {b}, 0, 1);
    }

    /**
     * バイト列を追加.
     * @param index 位置
     * @param b バイト列
     */
    @Override
    public void add(long index, byte[] b) {
        add(index, b, 0, b.length);
    }

    /**
     * 1バイト削除
     * @param index 位置
     * @return 削除したデータ
     */
    @Override
    public byte del(long index) {
        byte[] d = new byte[1];
        del(index, d, 0, 1);
        return d[0];
    }

    /**
     * 削除 / 切り取り
     * @param index 位置
     * @param d 削除されたデータ, サイズ
     * @return これ
     */
    @Override
    public Edit del(long index, byte[] d) {
        return del(index, d, 0, d.length);
    }
    
    /**
     * 上書き
     * EditBlockは上限はないことにする.
     * @param src 転送元
     * @return 転送サイズ = src.remaining
     */
    @Override
    public int write(ByteBuffer src) {
        if ( src.hasArray() ) { // 中間が要らない実装
            int p = src.position();
            int r = src.remaining();
            write(src.array(), src.arrayOffset() + p, r);
            src.position(p + r);
            return r;
        } else {
            byte[] d = new byte[src.remaining()];
            src.get(d);
            write(d, 0, d.length);
            return d.length;
        }
    }
}
