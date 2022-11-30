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
 *
 */
public class RevInputStream extends InputStream implements RevInput {
    
    final RevInput in;
    
    public RevInputStream(RevInput in) {
        this.in = in;
    }

    /**
     * 逆に繋げるかもしれないので仮
     * @return 
     */
    @Override
    public int read() {
        return in.backRead();
    }
    
    /**
     * 読む.
     * @param d データ入れ
     * @return サイズ
     */
    @Override
    public int read(byte[] d) {
        return in.backRead(d);
    }
    
    @Override
    public int read(byte[] d, int offset, int length) {
        return in.backRead(d, offset, length);
    }

    @Override
    public RevInputStream getBackInputStream() {
        return this;
    }

    @Override
    public int backRead() {
        return in.backRead();
    }

    /**
     * 後ろ(offset + length)から詰める.
     * 
     * @param d 入れ物
     * @param offset 書ける位置の頭
     * @param length 書ける領域の大きさ
     * @return 
     */
    @Override
    public int backRead(byte[] d, int offset, int length) {
        return in.backRead(d, offset, length);
    }

    @Override
    public int backRead(byte[] d) {
        return in.backRead(d, 0, d.length);
    }

    /**
     * 逆読みできるサイズを32bitに丸めたもの.
     * 最大 Integer.MAX_VALUE
     * @return 逆読みできるサイズ
     */
    @Override
    public int backSize() {
        return in.backSize();
    }

    /**
     * どれくらい逆読みできるか
     * @return 逆読みできるサイズ
     */
    @Override
    public long backLength() {
        return in.backLength();
    }

    @Override
    public Packet backReadPacket(long length) {
        return in.backReadPacket(length);
    }

    @Override
    public long back(long length) {
        return in.back(length);
    }

    @Override
    public byte revGet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
