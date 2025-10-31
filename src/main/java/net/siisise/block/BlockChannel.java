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
package net.siisise.block;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Channelに擬態するBlock.
 * size() が違うのでラップ型で実装する
 */
public class BlockChannel implements SeekableByteChannel {
    
    private OverBlock block;
    
    BlockChannel(OverBlock b) {
        block = b;
    }

    /**
     * ここからバイト列をdstに読み込む.
     * @param dst 転送先
     * @return サイズ的なもの
     * @throws IOException 
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        long bl = block.backLength();
        new ByteBufferBlock(dst).write(block);
        long n = block.backLength();
        return (int)(n - bl);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        long bl = block.backLength();
        block.write(new ByteBufferBlock(src));
        long n = block.backLength();
        return (int)(n - bl);
    }

    @Override
    public long position() throws IOException {
        return block.backLength();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        block.seek(newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        return block.length();
    }

    /**
     * 詰める.
     * EditBlock は詰め.
     * OverBlock の場合は subブロックで茶を濁す
     * @param size 全体のサイズ
     * @return 縮めたブロック
     * @throws IOException some I/O error
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if ( block.backLength() + block.length() > size ) {
            long p = Math.min(block.backLength(), size);
            if ( block instanceof EditBlock ) {
                ((EditBlock)block).del(size, block.backLength() + block.length() - size);
            } else {
                block = block.sub(0, size);
            }
            block.seek(p);
        }
        return this;
    }

    /**
     * このチャンネルがOpenされているかどうか.
     * @return openならtrue
     */
    @Override
    public boolean isOpen() {
        return block != null;
    }

    @Override
    public void close() throws IOException {
        if ( block instanceof Closeable ) {
            ((Closeable)block).close();
            block = null;
        }
    }
    
}
