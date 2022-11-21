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
import net.siisise.block.OverBlock;

/**
 * Abstract的な
 */
public abstract class AbstractInput extends InputStream implements FrontInput {

    @Override
    public byte get() {
        byte[] b = new byte[1];
        get(b,0,1);
        return (byte)b[0];
    }
    
    @Override
    public AbstractInput get(byte[] b) {
        return get(b, 0, b.length);
    }
    
    @Override
    public AbstractInput get(byte[] b, int offset, int length) {
        int len = size();
        if ( len < length ) {
            throw new java.nio.BufferUnderflowException();
        }
        int s = read(b,offset, length);
        if ( s < 0 ) {
            throw new java.nio.BufferUnderflowException();
        }
        return this;
    }
    
    @Override
    public AbstractInput get(OverBlock bb) {
        bb.write(this);
        return this;
    }
    
    @Override
    public int read() {
        byte[] d = new byte[1];
        int s = read(d);
        return s < 1 ? -1 : d[0] & 0xff;
    }

    @Override
    public InputStream getInputStream() {
        return this;
    }

    @Override
    public int read(byte[] d) {
        return read(d, 0, d.length);
    }

    /**
     * ないときは サイズ0
     *
     * @param buf バッファ
     * @param offset バッファ位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    @Override
    public abstract int read(byte[] buf, int offset, int length);

    /**
     * バイト列にする.
     * @return available() な中身
     */
    @Override
    public byte[] toByteArray() {
        byte[] b = new byte[size()];
        read(b);
        return b;
    }
    
    /**
     * InputStream 用サイズ.
     * @return size
     */
    @Override
    public int available() {
        return size();
    }

    /**
     * 読まずに進む.
     * @param length 相対サイズ
     * @return 移動したサイズ
     */
    @Override
    public long skip(long length) {
        return Input.skipImpl(this, length);
    }

    /**
     * PacketAを使った簡易実装.
     *
     * @param length
     * @return
     */
    @Override
    public Packet split(long length) {
        PacketA pac = new PacketA();
        pac.write(this, length);
        return pac;
    }

    @Override
    public int size() {
        return (int)Math.min(length(), Integer.MAX_VALUE);
    }
}
