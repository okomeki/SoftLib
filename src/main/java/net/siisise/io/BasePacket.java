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
 * Packetの軽く共通部分.
 */
public abstract class BasePacket extends Edit implements Packet {

    @Override
    public abstract BasePacket del(long index, byte[] buf, int offset, int length);
    /**
     * エラーなしで書き込む.
     * @param b データ
     * @param offset 位置
     * @param length サイズ
     * @return これ
     */
    @Override
    public Base put(byte[] b, int offset, int length) {
        write(b, offset, length);
        return this;
    }

    @Override
    public long backLength() {
        return length();
    }
}
