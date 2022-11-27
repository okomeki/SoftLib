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
 * 逆読み. Reverse Input
 * 標準ではないところから読む.
 * 分割された順序に注意。
 */
public interface RevInput {
    
    InputStream getBackInputStream();

    byte revGet();

    /**
     * 1バイト逆から読む.
     * 読めないときは -1
     * @return 1バイト または -1
     */
    int backRead();

    /**
     * 逆から読む.
     * 短い場合は後ろ(offset + length)から詰める.
     * @param buf バッファ
     * @param offset バッファ位置
     * @param length 長さ
     * @return 読めた長さ
     */
    int backRead(byte[] buf, int offset, int length);
    int backRead(byte[] buf);
    /**
     * 逆読み
     * @param length
     * @return 
     */
    Packet backSplit(long length);
    /**
     * Input#skip(long) の逆
     * @param length 読みとばすサイズ skip size.
     * @return skip size;
     */
    long back(long length);

    public static Packet splitImpl(RevInput in, long length) {
        Packet pac = new PacketA();
        RevOutput.backWrite(pac, in, length);
        return pac;
    }

    /**
     * 実装用。物理移動が伴う場合。
     * ポインタのみの場合は別のがいい
     * @param in 入力元
     * @param length 長さ
     * @return 読めた長さ
     */
    public static long backImpl(RevInput in, long length) {
        long r = length;
        byte[] t = new byte[(int) Math.min(length, PacketA.MAXLENGTH)];
        while (r > 0 && in.backSize() > 0) {
            int s = in.backRead(t, 0, (int) Math.min(r, t.length));
            if (s <= 0) {
                break;
            }
            r -= s;
        }
        return length - r;
    }
    
    /**
     * RevInputで読めるサイズ.
     * position 的な位置づけ
     * @return size 戻って読めるサイズ
     */
    long backLength();

    /**
     * RevInput で読めるサイズ.
     * position.
     * intを超える場合は Integer#MAX_LENGTH
     * @return size
     */
    int backSize();
}
