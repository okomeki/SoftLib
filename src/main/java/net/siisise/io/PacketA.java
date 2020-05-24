package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * First In First Out Stream Packet.
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

        PacketIn(byte[] data) {
            prev = this;
            next = this;
            this.data = data;
            offset = 0;
            length = data.length;
        }

        /**
         * this = B pac = D this A // B pac C // D B.prev = C C.next = B D.prev
         * = A pac が nextのとき 自分が輪から切れる
         *
         * @param pac
         */
        void addPrev(PacketIn pac) {
            prev.next = pac;
            pac.prev.next = this;
            PacketIn pre = pac.prev;
            pac.prev = prev;
            prev = pre;
        }

        void delete() {
//            addPrev(next);
            next.prev = prev;
            prev.next = next;
        }

    }

    PacketIn nullPack = new PacketIn();

    /**
     * ちょっと違うので分けたい (未使用?)
     */
    enum Mode {
        /** 1バイト待たない. */
        STREAM,
        /** 1バイトデータが用意されるまでブロックする? */
        BLOCK,
        /** 終了しました. */
        EOF,
        CLOSE
    }

    /**
     * InputStream との違い 1バイト待たない
     */
    private class PacketBaseInputStream extends InputStream {

        PacketIn base;
        Mode mode = Mode.STREAM;

        PacketBaseInputStream(PacketIn nullPac) {
            base = nullPac;
        }

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

        @Override
        public int read(byte[] b, int offset, int length) {
            PacketIn n;
            int len = 0;
            while (base.next != nullPack) {
                n = base.next;
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
            mode = Mode.EOF;
            return len;
        }

        @Override
        public int available() {
            return size();
        }
    }

    private class PacketBackInputStream extends InputStream {

        PacketIn base;
        Mode mode = Mode.STREAM;

        PacketBackInputStream(PacketIn nullPac) {
            base = nullPac;
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
            while (base.prev != nullPack) {
                n = base.prev;
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
            mode = Mode.EOF;
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
                nullPack.addPrev(new PacketIn(d));
                length -= MAXLENGTH;
                offset += MAXLENGTH;
            }
            if (length > 0) {
                d = new byte[length];
                System.arraycopy(src, offset, d, 0, length);
                nullPack.addPrev(new PacketIn(d));
            }
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
            while (length > MAXLENGTH) {
                d = new byte[MAXLENGTH];
                System.arraycopy(src, offset, d, 0, MAXLENGTH);
                nn.addPrev(new PacketIn(d));
                length -= MAXLENGTH;
                offset += MAXLENGTH;
            }
            if (length > 0) {
                d = new byte[length];
                System.arraycopy(src, offset, d, 0, length);
                nn.addPrev(new PacketIn(d));
            }
        }
    }

    PacketBaseInputStream in;
    PacketBackInputStream bin;
    PacketBaseOutputStream out;
    PacketBackOutputStream bout;

    public PacketA() {
        in = new PacketBaseInputStream(nullPack);
        bin = new PacketBackInputStream(nullPack);
        out = new PacketBaseOutputStream();
        bout = new PacketBackOutputStream();
    }

    public PacketA(byte[] b) {
        in = new PacketBaseInputStream(nullPack);
        bin = new PacketBackInputStream(nullPack);
        out = new PacketBaseOutputStream();
        bout = new PacketBackOutputStream();
        write(b);
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    public InputStream getBackInputStream() {
        return bin;
    }

    @Override
    public OutputStream getOutputStream() {
        return out;
    }
    
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
}
