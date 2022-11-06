/*
 * Copyright 2022 okomeki.
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
 * First In First Out Stream Packet.
 * 参照型リストのようなもの
 * 4回目くらいの実装
 */
public class PacketA extends BasePacket {

    /**
     * 効率的に開放したいので内部で分割する最大値など設けてみる
     */
    static final int MAXLENGTH = 0x1000000;

    private static class PacketIn {

        PacketIn prev;
        PacketIn next;
        byte[] data;
        int offset;
        /**
         * 実質サイズ
         */
        int length;

        PacketIn() { // NULLPACK
            prev = this;
            next = this;
            offset = 0;
            length = 0;
        }

        private PacketIn(byte[] data) {
            prev = this;
            next = this;
            this.data = data;
            offset = 0;
            length = data.length;
        }

        final void addPrev(byte[] data) {
            PacketIn pac = new PacketIn(data);
            addPrev(pac);
        }

        /**
         * PacketInをリングに追加する.
         * 二つの地点の組み合わせを交換するので追加、切り取りにも使える.
         * this の手前と pac の手前を入れ換える.
         * 
         * this A -> B
         * block C -> D
         * this = B
         * block = D
          
         * B.prev = C
         * C.next = B
         * D.prev = A
         * 
         * block が nextのとき 自分が輪から切れる
         * 
         * @param pac D
         */
        final void addPrev(PacketIn pac) {
            prev.next = pac;            // A の次は D
            pac.prev.next = this;       // C の次は B
            PacketIn pre = pac.prev;    // pre = C
            pac.prev = prev;            // D の前は A
            prev = pre;                 // B の前は C
        }

        /**
         * 自分から前後の参照は残しつつ切り離す.
         * 隣も消えると残す効果はない.
         */
        private void delete() {
            next.prev = prev;
            prev.next = next;
        }

    }

    final PacketIn nullPack = new PacketIn();

    /**
     * ちょっと違うので分けたい (未使用?)
     */
    enum Mode {
        /** 1バイト待たない. */
        STREAM,
        /**
         * 1バイトデータが用意されるまでブロックする? 
         * 未実装
         */
        BLOCK,
        /** 終了しました. */
        EOF,
        CLOSE
    }

    /**
     * Input / Output を中で持つので手軽に使える.
     */
    public PacketA() {
    }

    public PacketA(byte[] b) {
        write(b);
    }

    @Override
    public int read(byte[] b, int offset, int length) {
        PacketIn n;
        int len = 0;
        while (nullPack.next != nullPack) {
            n = nullPack.next;
            if (length >= n.length) {
                System.arraycopy(n.data, n.offset, b, offset, n.length);
                length -= n.length;
                offset += n.length;
                len += n.length;
                n.delete();
            } else {
                System.arraycopy(n.data, n.offset, b, offset, length);
                n.length -= length;
                n.offset += length;
                return len + length;
            }
        }
        // 
//        mode = Mode.EOF;
        return len;
    }

    @Override
    public int backRead(byte[] b, int offset, int length) {
        PacketIn n;
        int len = 0;
        while (nullPack.prev != nullPack) {
            n = nullPack.prev;
            if (length >= n.length) {
                System.arraycopy(n.data, n.offset, b, offset + length - n.length, n.length);
                length -= n.length;
                len += n.length;
                n.delete();
            } else {
                System.arraycopy(n.data, n.offset + n.length - length, b, offset, length);
                n.length -= length;
                len += length;
                return len;
            }
        }
        // 
//            mode = Mode.EOF;
        return len;
    }

    /**
     * 中身の移動.
     * @param pac null不可
     */
    @Override
    public void write(Input pac) {
        if (pac instanceof PacketA) {
            PacketIn an = ((PacketA)pac).nullPack;
            nullPack.addPrev(an);
            an.addPrev(an.next); // an を nullPack のみにする
        } else {
            Output.write(this, pac, pac.length());
        }
    }

    /**
     * PacketA 以外のsplit で使いやすそうな形
     * @param pac 入力
     * @param length
     * @return 移動したサイズ
     */
    public long write(Input pac, long length) {
        if (pac instanceof PacketA) {
            Packet an = ((PacketA)pac).split(length);
            long r = an.length();
            write(an);
            return r;
        } else {
            return Output.write(this, pac, length);
        }
    }

    /**
     * ToDo: まとめて変換してから追加してもいい
     *
     * @param src
     * @param offset
     * @param length
     */
    @Override
    public void write(byte[] src, int offset, int length) {
        PacketIn pp = nullPack.prev;
        if (pp != nullPack && length > 0 && pp.offset + pp.length + length < pp.data.length ) {
            System.arraycopy(src, offset, pp.data, pp.offset + pp.length, length);
            pp.length += length;
            return;
        }
        while (length > 0) {
            byte[] d = new byte[Math.min(length, MAXLENGTH)];
            System.arraycopy(src, offset, d, 0, d.length);
            nullPack.addPrev(d);
            length -= d.length;
            offset += d.length;
        }
    }
    
    @Override
    public void dwrite(byte[] d) {
        nullPack.addPrev(d);        
    }

    @Override
    public void backWrite(byte[] src, int offset, int length) {
        PacketIn nn = nullPack.next;
        if (length > 0 && nn.offset >= length) { // 空いているところに詰め込むことにしてみたり nullPackはoffset 0なので判定しなくて問題ない
            System.arraycopy(src, offset, nn.data, nn.offset - length, length);
            nn.offset -= length;
            nn.length += length;
            return;
        }
        byte[] d;
        while (length > 0) {
            d = new byte[Math.min(length, MAXLENGTH)];
            System.arraycopy(src, offset, d, 0, d.length);
            nn.addPrev(d);
            length -= d.length;
            offset += d.length;
        }
    }

    @Override
    public void dbackWrite(byte[] d) {
        nullPack.next.addPrev(d);
    }

    @Override
    public long length() {
        long length = 0;
        PacketIn pc = nullPack.next;
        while (pc != nullPack) {
            length += pc.length;
            pc = pc.next;
        }
        return length;
    }
    
    @Override
    public String toString() {
        return super.toString() + "length:" + Long.toString(length());
    }
    
    /**
     * 試験的
     */
    @Override
    public void flush() {
        PacketIn nn;
        if ( nullPack.next == nullPack ) return;
        for (nn = nullPack.next; nn.next != nullPack; nn = nn.next) {
            PacketIn nx = nn.next;
            if ( nn.data.length + nx.data.length < 1024 ) {
                byte[] d = new byte[nn.data.length + nx.data.length];
                System.arraycopy(nn.data, nn.offset, d, 0, nn.length);
                System.arraycopy(nx.data, nx.offset, d, nn.length, nx.length);
                nx.addPrev(new PacketIn(d));
                nx.delete();
                nn.delete();
//                System.err.println("gc 1");
            } else if ( nn.length < nn.next.offset ) {
                System.arraycopy(nn.data, nn.offset, nn.next.data, nn.next.offset - nn.length, nn.length);
                nn.next.offset -= nn.length;
                nn.next.length += nn.length;
                nn.delete();
//                System.err.println("gc 2");
            } else if ( nn.data.length > nn.offset + nn.length + nn.next.length ) {
                System.arraycopy(nn.next.data, nn.next.offset, nn.data, nn.offset + nn.length, nn.next.length);
                nn.length += nn.next.length;
                nn.next.delete();
//                System.err.println("gc 3");
            }
        }
    }

    /**
     * length で半分に分けて前半を返す.
     * ちょっと分割.
     * readPacket でもいい.
     * @param length 長さ
     * @return 
     */
    @Override
    public PacketA split(long length) {
        long limit = length;
        PacketA newPac = new PacketA();
        PacketIn n = nullPack.next; // read 方向
        while ( n != nullPack && n.length <= limit ) {
            limit -= n.length;
            n = n.next;
        }
        // n は packの頭
        if ( nullPack.next != n ) {
            PacketIn c = nullPack.next; // c = 新データの頭
            n.addPrev(c);
            newPac.nullPack.addPrev(c);
        }

        if ( limit > 0 ) {
            byte[] d = new byte[(int)Long.min(limit, size())];
            read(d);
            newPac.dwrite(d);
        }
        return newPac;
    }

    /**
     * length で半分に分けて後半を返す.
     * @param length
     * @return 後半
     */
    @Override
    public PacketA backSplit(long length) {
        long limit = length;
        PacketA newPac = new PacketA();
        PacketIn n = nullPack.prev; // write 方向
        while ( n != nullPack && n.length <= limit ) {
            limit -= n.length;
            n = n.prev;
        }
        // n は packの最後
        if ( nullPack.prev != n ) {
            PacketIn c = n.next; // c = 新データの頭
            nullPack.addPrev(c);
            newPac.nullPack.addPrev(c);
        }

        if ( limit > 0 ) {
            byte[] d = new byte[(int)Long.min(limit, size())];
            backRead(d);
            newPac.dbackWrite(d);
        }
        return newPac;
    }
    
    @Override
    public long skip(long length) {
        Packet p = split(length);
        return p.length();
    }

    /**
     * backSkip
     * @param length
     * @return 
     */
    @Override
    public long back(long length) {
        Packet p = backSplit(length);
        return p.length();
    }

    @Override
    public IndexInput get(long index, byte[] b, int offset, int length) {
        if ( length() < length) {
            throw new java.nio.BufferOverflowException();
        }
        PacketA bb = backSplit(length() - index - length);
        PacketA t = backSplit(length);
        t.read(b, offset, length);
        write(b, offset, length);
        write(bb);

        read(b,offset,length);
        return this;
    }

    /**
     * 上書き overwrite
     * @param index
     * @param b 
     * @param offset 
     * @param length 
     */
    @Override
    public void put(long index, byte[] b, int offset, int length) {
        PacketA bb = backSplit(length() - index);
        bb.split(length);
        bb.backWrite(b, offset, length);
        write(bb);
    }

    /**
     * 追加
     * @param index
     * @param b
     * @param offset
     * @param length 
     */
    @Override
    public void add(long index, byte[] b, int offset, int length) {
        PacketA bb = backSplit(length() - index);
        write(b, offset, length);
        write(bb);
    }

    /**
     * 1バイトだけ消す.
     * drop とかぶるかも
     * @param index
     * @return 
     */
    @Override
    public byte del(long index) {
        byte[] b = new byte[1];
        del(index, b, 0, 1);
        return b[0];
    }

    /**
     * 切り取り。データが要らない場合.
     * @param index 位置
     * @param length 長さ
     */
    @Override
    public void del(long index, long length) {
        PacketA bb = backSplit(length() - index - length);
        backSplit(length);
        write(bb);
    }

    /**
     * 切り取り.
     * @param index 位置
     * @param b データ入れ、サイズ
     * @return this
     */
    @Override
    public PacketA del(long index, byte[] b) {
        return del(index, b, 0, b.length);
    }

    /**
     * 切り取り。
     * @param index 位置
     * @param b データ入れ
     * @param offset 位置
     * @param length サイズ
     * @return this
     */
    @Override
    public PacketA del(long index, byte[] b, int offset, int length) {
        PacketA bb = backSplit(length() - index);
        backRead(b, offset, length);
        write(bb);
        return this;
    }
}
