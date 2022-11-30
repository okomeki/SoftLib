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

import net.siisise.io.BasePacket;

/**
 * ByteBlock の集合.
 * テスト実装.
 */
public class BlockPacket extends BasePacket {

    private static class BlockIn {
        private BlockIn prev = this;
        private BlockIn next = this;
        // data, offset, length の代わり
        private OverBlock block;
        
        private BlockIn() {
        }
        
        private BlockIn(OverBlock b) {
            block = b;
        }
        
        void addPrev(OverBlock b) {
            excPrev(new BlockIn(b));
        }
        
        /**
         * PacketA PacketIn と同じ原理
         * @param pac 
         */
        void excPrev(BlockIn pac) {
            prev.next = pac;            // A の次は D
            pac.prev.next = this;       // C の次は B
            BlockIn pre = pac.prev;    // pre = C
            pac.prev = prev;            // D の前は A
            prev = pre;                 // B の前は C
            
        }
        
        void delete() {
            prev.next = next;
            next.prev = prev;
        }
    }
    
    private BlockIn nullBlock = new BlockIn();

    /**
     * ないときは サイズ0
     *
     * @param d バッファ
     * @param offset バッファ位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    @Override
    public int read(byte[] d, int offset, int length) {
        int b = length;
        while ( nullBlock.next != nullBlock && length > 0 ) {
            BlockIn n = nullBlock.next;
            int s = n.block.read(d,offset,length);
            offset += s;
            length -= s;
            if ( n.block.length() == 0 ) {
                n.delete();
            }
        }
        return b - length;
    }

    /**
     * 読める長さ
     */
    @Override
    public long length() {
        long len = 0;
        for ( BlockIn n = nullBlock.next; n != nullBlock; n = n.next) {
            len += n.block.length();
        }
        return len;
    }

    /**
     * 逆読み
     * @param buf バッファ
     * @param offset バッファ位置
     * @param length サイズ
     * @return 読めたサイズ
     */
    @Override
    public int backRead(byte[] buf, int offset, int length) {
        int x = length;
        for ( BlockIn n = nullBlock.prev; n != nullBlock && x > 0; n = n.prev ) {
            int min = x < n.block.size() ? x : n.block.size();
            n.block.backRead(buf, offset + x - min, min);
            x -= min;
            if ( n.block.length() == 0 ) {
                n.delete();
            }
        }
        return length - x;
    }

    @Override
    public void backWrite(byte[] src, int offset, int length) {
        byte[] cp = new byte[length];
        System.arraycopy(src, offset, cp, 0, length);
        nullBlock.next.addPrev(OverBlock.wrap(cp));
    }

    @Override
    public void write(byte[] src, int offset, int length) {
        byte[] cp = new byte[length];
        System.arraycopy(src, offset, cp, 0, length);
        nullBlock.addPrev(OverBlock.wrap(cp));
    }

    @Override
    public BlockPacket get(long index, byte[] b, int offset, int length) {
        if ( length() < length ) {
            throw new java.nio.BufferOverflowException();
        }
        BlockIn n;
        for ( n = nullBlock.next; n != nullBlock && index >= n.block.length(); n = n.next ) {
            index -= n.block.length();
        }
        if ( n != nullBlock && index > 0 ) {
            byte[] nb = new byte[(int)index];
            n.block.read(nb);
            n.addPrev(OverBlock.wrap(nb)); // 部分的には残せないので分ける
            
        }
        while ( n != nullBlock && length > 0 ) {
            int s = n.block.read(b,offset,length);
            offset += s;
            length -= s;
            if ( n.block.length() == 0 ) {
                n.delete();
            }
            n = n.next;
        }
        if ( n != nullBlock && n.prev != nullBlock && n.block.backLength() > n.prev.block.length() ) {
            byte[] t = new byte[n.prev.block.size()];
            n.prev.block.read(t);
            n.block.backWrite(t);
        }
        return this;
    }

    /**
     * 指定位置に上書き.
     * @param index 位置
     * @param d データ
     * @param offset データ位置
     * @param length サイズ
     */
    @Override
    public void put(long index, byte[] d, int offset, int length) {
        if ( length() < length ) {
            throw new java.nio.BufferOverflowException();
        }
        BlockIn n;
        for ( n = nullBlock.next; n != nullBlock && index >= n.block.length(); n = n.next ) {
            index -= n.block.length();
        }
        while ( n != nullBlock && length > 0 ) { // 上書き
            long s = n.block.backLength();
            int min = length < n.block.size() ? length : n.block.size();
            n.block.put(s + index, d, offset, min);
            index = 0;
            offset += min;
            length -= min;
            n = n.next;
        }
        if ( length > 0 ) { // 増える
            byte[] t = new byte[length];
            System.arraycopy(d, offset, t, 0, length);
            n.addPrev(OverBlock.wrap(t));
        }
    }

    @Override
    public void del(long index, long size) {
        if ( length() < index + size ) {
            throw new java.nio.BufferOverflowException();
        }
        BlockIn n;
        for ( n = nullBlock.next; n != nullBlock && index >= n.block.length(); n = n.next ) {
            index -= n.block.length();
        }
        if ( n != nullBlock && index > 0 ) {
            byte[] nb = new byte[(int)index];
            n.block.read(nb);
            n.addPrev(OverBlock.wrap(nb)); // 部分的には残せないので分ける
        }
        while ( n != nullBlock && n.block.length() <= size ) { // ざっくり消す
            n.delete();
            size -= n.block.length();
            n = n.next;
        }
        if ( n != nullBlock && size > 0 ) {
            n.block.skip(size);
        }
    }

    /**
     * 手抜き.
     * @param index 位置
     * @param buf バックアップ
     * @param offset bufの位置
     * @param length サイズ
     * @return これ
     */
    @Override
    public BlockPacket del(long index, byte[] buf, int offset, int length) {
        get(index, buf, offset, length);
        del(index, length);
        return this;
    }

    @Override
    public void add(long index, byte[] d, int offset, int length) {
        if ( length() < index ) {
            throw new java.nio.BufferOverflowException();
        }
        BlockIn n;
        for ( n = nullBlock.next; n != nullBlock && index >= n.block.length(); n = n.next ) {
            index -= n.block.length();
        }
        if ( n != nullBlock && index > 0 ) {
            byte[] nb = new byte[(int)index];
            n.block.read(nb);
            n.addPrev(OverBlock.wrap(nb)); // 部分的には残せないので分ける
        }
        n.addPrev(OverBlock.wrap(d,offset,length));
    }

}
