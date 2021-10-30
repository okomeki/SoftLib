package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * ビット操作用.
 *
 * 上位ビット優先(Big Endian)/下位ビット(Little Endian)優先共通実装
 */
public abstract class BaseBitPac implements BitPacket {

    protected Packet pac = new PacketA();

    /**
     * 前方空白量
     */
    int readPadding;
    /**
     * 後方空白量
     */
    int writePadding;

    abstract public class BitInputStream extends InputStream {

        @Override
        public int read() {
            if (bitLength() < 8) {
                return -1;
            }
            return readInt(8);
        }

        @Override
        public int read(byte[] data) {
            return read(data, 0, data.length);
        }

        /**
         * 読み込んだ次のバイトが壊れていない保証はない
         *
         * @param data
         * @param offset
         * @param length
         * @return
         */
        @Override
        public int read(byte[] data, int offset, int length) {
            if (length > bitLength() / 8) {
                length = (int) (bitLength() / 8);
            }
            long len = readBit(data, ((long) offset) * 8, ((long) length) * 8l);
            return (int) (len / 8);
        }

        public abstract int readInt(int bit);

        public abstract long readBit(byte[] data, long offset, long bitLength);
        public abstract BitPacket readPac(int bitLength);

        @Override
        public int available() {
            return size();
        }
    }

    abstract public class BitOutputStream extends OutputStream {

        @Override
        public void write(int data) {
            writeBit(new byte[]{(byte) data}, (long) 0, (long) 8);
        }

        @Override
        public void write(byte[] data) {
            writeBit(data, 0, data.length * 8);
        }

        @Override
        public void write(byte[] data, int offset, int length) {
            writeBit(data, offset * 8, length * 8);
        }

        public void writeBit(BitPacket pac) {
            writeBit(pac, pac.bitLength());
        }

        /**
         * ToDo: 端数のbig/littleが異なる場合
         * @param bp
         * @param bitLength 
         */
        public void writeBit(BitPacket bp, long bitLength) {
            byte[] data = new byte[(int) (bitLength + 7) / 8];
            bp.readBit(data, 0, bitLength);
            writeBit(data, 0, bitLength);
        }

        public abstract void writeBit(int data, int bitLength);

        public abstract void writeBit(byte[] data, long bitOffset, long bitLength);
    }

    protected BitInputStream in;
    protected BitOutputStream out;
    protected BitInputStream backIn;
    protected BitOutputStream backOut;

    @Override
    public BitInputStream getInputStream() {
        return in;
    }

    @Override
    public BitOutputStream getOutputStream() {
        return out;
    }

    @Override
    public BitInputStream getBackInputStream() {
        return backIn;
    }

    @Override
    public BitOutputStream getBackOutputStream() {
        return backOut;
    }

    @Override
    public long bitLength() {
        return pac.length() * 8 - readPadding - writePadding;
    }

    @Override
    public long length() {
        return bitLength() / 8;
    }

    @Override
    public int size() {
        long l = length();
        return l > ((long) Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) l;
    }

    /**
     *
     * @param len
     * @return
     */
    static final int andMask(int len) {
        //if ( len == 32 ) return -1;
        return (int) (1l << len) - 1;
    }

    /**
     * 1バイトリード。
     * 8ビット以上有効な場合に読み出し可能.
     *
     * @return 下位8ビットで1バイト -1でデータなし.
     */
    @Override
    public int read() {
        return in.read();
    }

    /**
     * 8ビット単位で転送. 端数は残る。
     *
     * @param data 転送先配列
     * @return 転送可能なバイト長。
     */
    @Override
    public int read(byte[] data) {
        return in.read(data, 0, data.length);
    }

    @Override
    public int read(byte[] data, int offset, int length) {
        return in.read(data, offset, length);
    }

    /**
     *
     * @param bit 1～32ぐらい
     * @return 下位ビットを指定ビット分上から埋める |xx012345|
     */
    @Override
    public int readInt(int bit) {
        return in.readInt(bit);
    }

    /**
     * readのビット版 入れ物はbyte列
     * 左詰め |01234567|89ABCDEF|
     *
     * @param data 戻りデータ
     * @param offsetBit 開始ビット位置
     * @param length ビット長
     * @return
     */
    @Override
    public long readBit(byte[] data, long offsetBit, long length) {
        return in.readBit(data, offsetBit, length);
    }

    @Override
    public BitPacket readPac(int length) {
        return in.readPac(length);
    }

    /**
     * 端数切り捨て.
     *
     * @return
     */
    @Override
    public byte[] toByteArray() {
        byte[] data = new byte[size()];
        read(data);
        return data;
    }

    @Override
    public int backRead() {
        return backIn.read();
    }

    @Override
    public int backRead(byte[] data) {
        return backIn.read(data, 0, data.length);
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        return backIn.read(data, offset, length);
    }

    @Override
    public int backReadInt(int bit) {
        return backIn.readInt(bit);
    }

    @Override
    public long backReadBit(byte[] data, long offsetBit, long length) {
        return backIn.readBit(data, offsetBit, length);
    }

    @Override
    public void write(int data) {
        out.write(new byte[]{(byte) data});
    }

    @Override
    public void write(byte[] data) {
        writeBit(data, 0, data.length * 8);
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        out.write(data, offset, length);
    }

    @Override
    public void writeBit(int data, int bitLength) {
        out.writeBit(data, bitLength);
    }

    @Override
    public void writeBit(BitPacket pac) {
        out.writeBit(pac);
    }

    @Override
    public void writeBit(BitPacket bp, long bitLength) {
        out.writeBit(bp, bitLength);
    }

    @Override
    public void writeBit(byte[] data, long bitOffset, long bitLength) {
        out.writeBit(data, bitOffset, bitLength);
    }

    @Override
    public void backWrite(int data) {
        backOut.write(new byte[]{(byte) data}, 0, 1);
    }

    @Override
    public void backWrite(byte[] data) {
        backOut.write(data, 0, data.length);
    }

    @Override
    public void dbackWrite(byte[] data) {
        backOut.write(data, 0, data.length);
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        backOut.write(data, offset, length);
    }

    @Override
    public void backWriteBit(int data, int bitLength) {
        backOut.writeBit(data, bitLength);
    }

    @Override
    public void backWriteBit(BitPacket pac) {
        backOut.writeBit(pac);
    }

    @Override
    public void backWriteBit(BitPacket pac, long bitLength) {
        backOut.writeBit(pac, bitLength);
    }

    @Override
    public void backWriteBit(byte[] data, long bitOffset, long bitLength) {
        backOut.writeBit(data, bitOffset, bitLength);
    }
}
