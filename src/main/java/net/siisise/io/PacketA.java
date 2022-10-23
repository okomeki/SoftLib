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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * First In First Out Stream Packet.
 * 参照型リストのようなもの
 * 4回目くらいの実装
 */
public class PacketA implements Packet {

    /**
     * 効率的に開放したいので内部で分割する最大値など設けてみる
     */
    static final int MAXLENGTH = 0x1000000;

    private class PacketIn {

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
         * 
         * this A -> B pac C -> D
         * this = B pac = D
          
         * B.prev = C
         * C.next = B
         * D.prev = A
         * 
         * pac が nextのとき 自分が輪から切れる
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
         * 自分から前後の参照は残しつつ切り離す
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
     * InputStream との違い 1バイト待たない
     */
    private class PacketBaseInputStream extends InputStream {

//        private Mode mode = Mode.STREAM;

        @Override
        public int read() {
            byte[] d = new byte[1];
            int len = read(d);
            if (len > 0) {
                return d[0] & 0xff;
            }
            return -1;
        }

        @Override
        public int read(byte[] b) {
            return read(b, 0, b.length);
        }

        /**
         * 
         * @param b
         * @param offset
         * @param length
         * @return 
         */
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
                    len += length;
                    return len;
                }
            }
            // 
//            mode = Mode.EOF;
            return len;
        }

        @Override
        public int available() {
            return size();
        }
    }

    private class PacketBackInputStream extends InputStream {

//        private Mode mode = Mode.STREAM;

        PacketBackInputStream() {
        }

        @Override
        public int read() {
            byte[] d = new byte[1];
            int len = read(d);
            if (len >= 0) {
                return d[0] & 0xff;
            }
            return -1;
        }

        @Override
        public int read(byte[] b) {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int offset, int length) {
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

        @Override
        public int available() {
            return size();
        }
    }

    private class PacketBaseOutputStream extends OutputStream {

        @Override
        public void write(int b) {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] b) {
            write(b, 0, b.length);
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
            byte[] d;
            while (length > MAXLENGTH) {
                d = new byte[MAXLENGTH];
                System.arraycopy(src, offset, d, 0, MAXLENGTH);
                nullPack.addPrev(d);
                length -= MAXLENGTH;
                offset += MAXLENGTH;
            }
            if (length > 0) {
                d = new byte[length];
                System.arraycopy(src, offset, d, 0, length);
                nullPack.addPrev(d);
            }
        }
        
        @Override
        public void flush() {
            gc();
        }
    }

    private class PacketBackOutputStream extends OutputStream {

        @Override
        public void write(int b) {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] b) {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] src, int offset, int length) {
            PacketIn nn = nullPack.next;
            byte[] d;
            if (length > 0 && nn.offset >= length) { // 空いているところに詰め込むことにしてみたり nullPackはoffset 0なので判定しなくて問題ない
                System.arraycopy(src, offset, nn.data, nn.offset - length, length);
                nn.offset -= length;
                nn.length += length;
                return;
            }
            while (length > MAXLENGTH) {
                d = new byte[MAXLENGTH];
                System.arraycopy(src, offset, d, 0, MAXLENGTH);
                nn.addPrev(d);
                length -= MAXLENGTH;
                offset += MAXLENGTH;
            }
            if (length > 0) {
                d = new byte[length];
                System.arraycopy(src, offset, d, 0, length);
                nn.addPrev(d);
            }
        }
        
        @Override
        public void flush() {
            gc();
        }
    }

    private final PacketBaseInputStream in;
    private final PacketBackInputStream bin;
    private final PacketBaseOutputStream out;
    private final PacketBackOutputStream bout;

    public PacketA() {
        in = new PacketBaseInputStream();
        bin = new PacketBackInputStream();
        out = new PacketBaseOutputStream();
        bout = new PacketBackOutputStream();
    }

    public PacketA(byte[] b) {
        this();
        write(b);
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public InputStream getBackInputStream() {
        return bin;
    }

    @Override
    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public OutputStream getBackOutputStream() {
        return bout;
    }

    @Override
    public int read() {
        return in.read();
    }

    @Override
    public int read(byte[] b, int offset, int length) {
        return in.read(b, offset, length);
    }

    @Override
    public int read(byte[] b) {
        return in.read(b);
    }

    @Override
    public int backRead() {
        return bin.read();
    }

    @Override
    public int backRead(byte[] b, int offset, int length) {
        return bin.read(b, offset, length);
    }

    @Override
    public int backRead(byte[] b) {
        return bin.read(b);
    }

    @Override
    public byte[] toByteArray() {
        byte[] d = new byte[size()];
        read(d);
        return d;
    }

    @Override
    public void write(FrontPacket pac) {
        if (pac instanceof PacketA) {
            PacketIn an = ((PacketA)pac).nullPack;
            nullPack.addPrev(an);
            an.addPrev(an.next);
        } else {
            write(pac.toByteArray());
        }
    }

    @Override
    public void write(int b) {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int offset, int length) {
        out.write(b, offset, length);
    }

    @Override
    public void write(byte[] b) {
        out.write(b);
    }
    
    public void dwrite(byte[] d) {
        nullPack.addPrev(d);        
    }

    @Override
    public void backWrite(int b) {
        bout.write(b);
    }

    @Override
    public void backWrite(byte[] b, int offset, int length) {
        bout.write(b, offset, length);
    }

    @Override
    public void backWrite(byte[] b) {
        bout.write(b);
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
    public int size() {
        long l = length();
        if ( l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)l;
    }

    @Override
    public String toString() {
        return super.toString() + "length:" + Long.toString(length());
    }
    
    /**
     * 試験的
     */
    public void gc() {
        PacketIn nn;
        for (nn = nullPack.next; nn.next != nullPack; nn = nn.next) {
            if ( nn.length < nn.next.offset ) {
                System.arraycopy(nn.data, nn.offset, nn.next.data, nn.next.offset - nn.length, nn.length);
                nn.next.offset -= nn.length;
                nn.next.length += nn.length;
                nn.delete();
                nn = nn.prev;
                System.err.println("gc 1");
            } else if ( nn.data.length > nn.offset + nn.length + nn.next.length ) {
                System.arraycopy(nn.next.data, nn.next.offset, nn.data, nn.offset + nn.length, nn.next.length);
                nn.length += nn.next.length;
                nn.next.delete();
                nn = nn.prev;
                System.err.println("gc 2");
            }
        }
    }

    /**
     * ちょっと分割.
     * @param length 長さ
     * @return 
     */
    public PacketA split(int length) {
        int limit = length;
        PacketA newPac = new PacketA();
        PacketIn n = nullPack.next;
        while ( n != nullPack && n.length <= limit ) {
            limit -= n.length;
            n = n.next;
        }
        if ( nullPack.next != n ) {
            PacketIn c = nullPack.next;
            n.addPrev(c);
            newPac.nullPack.addPrev(c);
        }
        
        if ( limit > 0 ) {
            byte[] d = new byte[length];
            int size = read(d);
            newPac.write(d, 0, size);
        }
        return newPac;
    }
}
