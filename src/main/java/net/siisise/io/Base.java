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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import net.siisise.block.EditBlock;
import net.siisise.block.OverBlock;
import net.siisise.math.Matics;

/**
 * Packet と Block の簡易実装.
 * 上書きはできるがサイズ変更の伴う処理はできない.
 */
public abstract class Base extends ReadBase implements FrontPacket, BackPacket, IndexOutput, ByteChannel {

    @Override
    public OutputStream getOutputStream() {
        return new FilterOutput(this);
    }

    @Override
    public OutputStream getBackOutputStream() {
        return new RevOutputStream(this);
    }

    @Override
    public Output put(byte b) {
        return put(new byte[] {b}, 0, 1);
    }

    @Override
    public Output put(byte[] b) {
        return put(b, 0, b.length);
    }

    @Override
    public void put(long index, byte d) {
        put(index, new byte[] {d}, 0, 1);
    }

    @Override
    public void put(long index, byte[] d) {
        put(index, d, 0, d.length);
    }

    @Override
    public void backWrite(int d) {
        backWrite(new byte[]{(byte) d}, 0, 1);
    }

    @Override
    public void backWrite(byte[] d) {
        backWrite(d, 0, d.length);
    }

    @Override
    public void dbackWrite(byte[] src) {
        backWrite(src, 0, src.length);
    }

    @Override
    public void flush() {
    }

    @Override
    public void write(int d) {
        write(new byte[] {(byte)d}, 0, 1);
    }

    @Override
    public void write(byte[] d) {
        write(d,0,d.length);
    }

    /**
     * データ移動 上限あり.
     * OverBlock の上限を超えない範囲で書き込む.
     * OverBlock / Packet 用実装かも.
     * @param src 元Buffer
     * @return 転送できたサイズ
     */
    @Override
    public int write(ByteBuffer src) {
        int moveLength = Math.min(src.remaining(), (this instanceof OverBlock ) ? size() : 0x10000000 );
        if ( src.hasArray() ) { // 中間が要らない実装
            int p = src.position();
            write(src.array(), src.arrayOffset() + p, moveLength);
            src.position(p + moveLength);
            return moveLength;
        } else {
            int l;
            int s = 0;
            while ( (l = Math.min(src.remaining(), (this instanceof OverBlock ) ? size() : 0x10000000 )) > 0 ) {
                byte[] d = new byte[l];
                src.get(d);
                write(d);
                s += d.length;
            }
            return s;
        }
    }


    @Override
    public void dwrite(byte[] data) {
        write(data, 0, data.length);
    }

    /**
     * Block系
     * @param pac 元
     * @return 移動したサイズ
     */
    @Override
    public long write(Input pac) {
        if ( !(this instanceof EditBlock) && (this instanceof OverBlock) ) {
            return Output.write(this, pac, Math.min(length(), pac.length()));
        } else {
            return Output.write(this, pac, pac.length());
        }
    }

    @Override
    public long write(Input pac, long length) {
        if ( pac.length() < length ) {
            throw new java.nio.BufferOverflowException();
        }
        if ( !(this instanceof EditBlock) && (this instanceof OverBlock) ) {
            if ( length() < length ) {
                throw new java.nio.BufferOverflowException();
            }
            return Output.write(this, pac, Matics.min(length(), pac.length(), length));
        } else {
            return Output.write(this, pac, Math.min(pac.length(), length));
        }
    }

    /**
     * 転送元、転送先どちらかの上限まで移動する.
     * 
     * @param pac
     * @return 
     */
    @Override
    public long backWrite(RevInput pac) {
        if ( !(this instanceof EditBlock) && (this instanceof OverBlock) ) {
            return RevOutput.backWrite(this, pac, Math.min(backLength(), pac.backLength()));
        } else {
            return RevOutput.backWrite(this, pac, pac.backLength());
        }
    }

    @Override
    public long backWrite(RevInput pac, long length) {
        if ( !(this instanceof EditBlock) && (this instanceof OverBlock) ) {
            return RevOutput.backWrite(this, pac, Matics.min(backLength(), pac.backLength(), length));
        } else {
            return RevOutput.backWrite(this, pac, Math.min(pac.backLength(), length));
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }
}
