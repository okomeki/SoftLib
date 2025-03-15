/*
 * Copyright 2019-2022 Siisise Net.
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
 * ビット操作用.
 * index系まだつかえない.
 *
 * 上位ビット優先(Big Endian)/下位ビット(Little Endian)優先共通実装
 */
public abstract class BaseBitPac extends BasePacket implements BitPacket {

    protected Packet pac = new PacketA();

    /**
     * 前方空白量
     */
    protected int readPadding;
    /**
     * 後方空白量
     */
    protected int writePadding;

    public abstract class BitInputStream extends AbstractInput {

        @Override
        public int read() {
            if (bitLength() < 8) {
                return -1;
            }
            return readInt(8);
        }

        /**
         * 読み込んだ次のバイトが壊れていない保証はない
         *
         * @param data 読み込み配列
         * @param offset　位置
         * @param length　読み込みサイズ
         * @return 読み込んだサイズ
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

        /**
         * ビット単位の読み込み.
         * Big Endian 左詰めまたは Little Endian 右詰め
         *
         * @param data 読み込み配列
         * @param offset ビット位置
         * @param bitLength　ビットサイズ
         * @return 読み込んだサイズ
         */
        public abstract long readBit(byte[] data, long offset, long bitLength);
        public abstract BitPacket readPac(int bitLength);

        @Override
        public long length() {
            return BaseBitPac.this.length();
        }

        @Override
        public boolean readable(long length) {
            return BaseBitPac.this.readable(length);

        }
    }

    public abstract class BitOutputStream extends AbstractOutput {

        @Override
        public void write(int d) {
            writeBit(new byte[]{(byte) d}, (long) 0, (long) 8);
        }

        @Override
        public void write(byte[] d) {
            writeBit(d, 0, d.length * 8);
        }

        @Override
        public void write(byte[] d, int offset, int length) {
            writeBit(d, offset * 8, length * 8);
        }

        @Override
        public Output put(byte[] data, int offset, int length) {
            writeBit(data, offset * 8, length * 8);
            return this;
        }

        public void writeBit(BitPacket pac) {
            writeBit(pac, pac.bitLength());
        }

        /**
         * ToDo: 端数のbig/littleが異なる場合
         *
         * @param bp
         * @param bitLength
         */
        public void writeBit(BitPacket bp, long bitLength) {
            byte[] data = new byte[(int) (bitLength + 7) / 8];
            bp.readBit(data, 0, bitLength);
            writeBit(data, 0, bitLength);
        }

        /**
         *
         * @param data
         * @param bitLength 0でも可
         */
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

    /**
     * 端数は含まないバイト列として返せる値.
     *
     * @return
     */
    @Override
    public long length() {
        return bitLength() / 8;
    }

    public boolean bitReadable(long length) {
        return pac.readable((length + readPadding + writePadding) / 8);
    }

    @Override
    public boolean readable(long length) {
        return bitReadable(length * 8);
    }

    /**
     *
     * @param len 0～32くらい
     * @return
     */
    static final int andMask(int len) {
        //if ( len == 32 ) return -1;
        return (int) (1l << len) - 1;
    }

    /**
     * 8ビット単位で転送.
     * 端数は残る。
     *
     * @param data 転送先配列
     * @return 転送可能なバイト長。
     */
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
     * readのビット版.
     * 入れ物はbyte列
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

    @Override
    public int backRead(byte[] buf, int offset, int length) {
        return backIn.read(buf, offset, length);
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
    public void backWrite(byte[] data, int offset, int length) {
        backOut.writeBit(data, offset * 8l, length * 8l);
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

    @Override
    public BaseBitPac get(long index, byte[] b, int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void put(long index, byte[] d, int srcOffset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(long index, byte[] d, int srcOffset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void del(long index, long size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseBitPac del(long index, byte[] d, int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
