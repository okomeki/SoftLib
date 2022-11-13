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
    static final int MAXLENGTH = 0x100000;

    /**
     * LinkedList な基本構造.
     */
    private static class PacketIn {

        private PacketIn prev = this;
        private PacketIn next = this;
        byte[] data;
        int offset;
        /**
         * 実質サイズ
         */
        int length;

        PacketIn() { // NULLPACK
            offset = 0;
            length = 0;
        }

        private PacketIn(byte[] data) {
            this.data = data;
            offset = 0;
            length = data.length;
        }

        final void addPrev(byte[] data) {
            excPrev(new PacketIn(data));
        }

        /**
         * PacketInをリングに追加する.
         * 二つの地点の組み合わせを交換するので追加、切り取りにも使える.
         * this の手前と pac の手前を入れ換える.
         * 
         * A ⇔ B this
         * C ⇔ D pac
         *
         * A → D
         * C → B
         * pre = C
         * 
         * A ← D
         * C ← B
         * 
         * block が nextのとき 自分が輪から切れる
         * 
         * @param pac D
         */
        final void excPrev(PacketIn pac) {
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
        int len = length;
        for ( PacketIn n = nullPack.next; n != nullPack; n = n.next ) {
            if (len >= n.length) {
                System.arraycopy(n.data, n.offset, b, offset, n.length);
                len -= n.length;
                offset += n.length;
            } else {
                System.arraycopy(n.data, n.offset, b, offset, len);
                n.length -= len;
                n.offset += len;
                n.excPrev(nullPack.next);
                return length;
            }
        }
//        mode = Mode.EOF;
        nullPack.excPrev(nullPack.next);
        return length - len;
    }

    /**
     * 逆読み.
     * 短い場合は b の後ろ( offset + length )から詰める
     * @param b 入れ物
     * @param offset b offset
     * @param length b length
     * @return 
     */
    @Override
    public int backRead(byte[] b, int offset, int length) {
        int len = length;
        for ( PacketIn n = nullPack.prev; n != nullPack; n = n.prev ) {
            if (len >= n.length) {
                len -= n.length;
                System.arraycopy(n.data, n.offset, b, offset + len, n.length);
            } else {
                n.length -= len;
                System.arraycopy(n.data, n.offset + n.length, b, offset, len);
                n.next.excPrev(nullPack);
                return length;
            }
        }
//            mode = Mode.EOF;
        nullPack.excPrev(nullPack.next);
        return length - len;
    }

    /**
     * 中身の移動.
     * @param pac null不可
     */
    @Override
    public void write(Input pac) {
        if (pac instanceof PacketA) {
            PacketIn an = ((PacketA)pac).nullPack;
            nullPack.excPrev(an);
            an.excPrev(an.next); // an を nullPack のみにする
        } else {
            Output.write(this, pac, pac.length());
        }
    }

    /**
     * 中身の移動.
     * @param pac null不可
     */
    public void backWrite(RevInput pac) {
        if (pac instanceof PacketA) {
            PacketIn an = ((PacketA)pac).nullPack;
            an.excPrev(nullPack);
            an.excPrev(an.next); // an を nullPack のみにする
        } else {
            RevOutput.backWrite(this, pac, pac.backLength());
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
/*
        if ( pp != nullPack && pp.data.length + length < 1024 ) {
            byte[] d = new byte[1024];
            System.arraycopy(pp.data, pp.offset, d, 0, pp.length);
            pp.data = d;
            pp.offset = 0;
        }
*/
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
/*
        if ( nn != nullPack && nn.data.length + length < 1024 ) {
            byte[] d = new byte[1024];
            System.arraycopy(nn.data, nn.offset, d, d.length - nn.length, nn.length);
            nn.offset = d.length - nn.length;
            nn.data = d;
        }
*/
        if (length > 0 && nn.offset >= length) { // 空いているところに詰め込むことにしてみたり nullPackはoffset 0なので判定しなくて問題ない
            System.arraycopy(src, offset, nn.data, nn.offset - length, length);
            nn.offset -= length;
            nn.length += length;
            return;
        }
        while (length > 0) {
            byte[] d = new byte[Math.min(length, MAXLENGTH)];
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
        //if ( nullPack.next == nullPack ) return;
        for (nn = nullPack.next; nn != nullPack && nn.next != nullPack; nn = nn.next) {
            PacketIn nx = nn.next;
            if ( nx != nullPack && nn.data.length + nx.data.length < 1024 ) {
                byte[] d = new byte[nn.length + nx.length];
                System.arraycopy(nn.data, nn.offset, d, 0, nn.length);
                System.arraycopy(nx.data, nx.offset, d, nn.length, nx.length);
                nx.excPrev(new PacketIn(d));
                nx.delete();
                nx = nn.next;
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
            n.excPrev(c);
            newPac.nullPack.excPrev(c);
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
     * read系の逆なのでwrite方向で読む.
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
            nullPack.excPrev(c);
            newPac.nullPack.excPrev(c);
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

    /**
     * 読んだところは消えない.
     * @param index
     * @param b
     * @param offset
     * @param length
     * @return 
     */
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
