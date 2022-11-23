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
 * size() が違うのでラップ型で実装する
 */
public class BlockChannel implements SeekableByteChannel {
    
    private OverBlock block;
    
    BlockChannel(OverBlock b) {
        block = b;
    }

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
     * 詰める subブロックで茶を濁す
     * @param size 全体のサイズ
     * @return 縮めたブロック
     * @throws IOException 
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        long p = block.backLength();
        block = block.sub(0, size);
        block.seek(p);
        return this;
    //    return new BlockChannel(block.sub(0, size));
    }

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
