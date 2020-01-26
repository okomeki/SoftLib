package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 上位ビットが先頭 |01234567|89abcdef|
 * byte列は左詰め、intは右詰め
 * @author okome
 */
public class BigBitPacket extends BaseBitPac {

    /**
     *
     * @param bit 1～32ぐらい
     * @return 下位ビットを指定ビット分上から埋める |xx012345|
     */
    @Override
    public int readInt(int bit) {
        if (bit > bitLength()) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        int ret = 0;
        int flen = 8 - readPadding;
        if (bit >= flen) { // そのまま
            ret = (int) (pac.read() & andMask(flen));
            bit -= flen;
            readPadding = 0;
        }
        int len = (int) bit / 8;
        if (len > 0) {
            byte[] d = new byte[len];
            pac.read(d);
            for (byte x : d) {
                ret <<= 8;
                ret |= x & 0xff;
            }
            bit -= len * 8;
        }
        if (bit > 0) { // bit = 1～7 ビット構造で異なる
            int d = pac.read();
            ret <<= bit;
            readPadding += bit;
            ret |= d >>> (8 - readPadding);
            if (pac.length() == 0 && readPadding + writePadding == 8) {
                readPadding = 0;
                writePadding = 0;
            } else {
                d &= andMask(8 - readPadding);
                pac.backWrite(d);
            }
        }
        return ret;
    }
    
    @Override
    public int backReadInt(int bit) {
        if (bit > bitLength()) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        int ret = 0;
        int flen = 8 - writePadding;
        if (bit >= flen) { // そのまま
            ret = (int) ((pac.backRead() >> writePadding) & andMask(flen));
            bit -= flen;
            writePadding = 0;
        }
        int len = (int) bit / 8;
        if (len > 0) {
            byte[] d = new byte[len];
            pac.backRead(d);
            for (byte x : d) {
                ret |= (x & 0xff) << flen;
                flen += 8;
            }
            bit -= len * 8;
        }
        if (bit > 0) { // bit = 1～7 ビット構造で異なる
            int d = ((pac.backRead() >> writePadding) & andMask(bit)) << flen;
            writePadding += bit;
            ret |= d;
            if (pac.length() == 0 && readPadding + writePadding == 8) {
                readPadding = 0;
                writePadding = 0;
            } else {
                d &= andMask(8 - writePadding) << bit;
                pac.write(d);
            }
        }
        return ret;
    }

    /**
     * 分ける
     * @param bit
     * @return 
     */
    @Override
    public BigBitPacket readPac(int bit) {
        if (bit > bitLength()) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        BigBitPacket bp = new BigBitPacket();
        if (bit >= (8 - readPadding)) { // そのまま
            int d;
            d = pac.read();
            bp.pac.write(d);
            bp.readPadding = readPadding;
            bit -= (8 - readPadding);
            readPadding = 0;
        } else {
            int d = readInt(bit);
            bp.writeBit(d, bit);
            return bp;
        }
        int len = (int) bit / 8;
        if (len > 0) {
            byte[] d = new byte[len];
            pac.read(d);
            bp.pac.write(d);
            bit -= len * 8;
        }
        if (bit > 0) { // bit = 1～7 ビット構造で異なる
            int d = readInt(bit);
            bp.writeBit(d,bit);
        }
        return bp;
    }

    /**
     * readのビット版 入れ物はbyte列
     * 左詰め |01234567|89ABCDEF|
     * @param data 戻りデータ
     * @param offsetBit 開始ビット位置
     * @param length ビット長
     * @return 
     */
    @Override
    public long readBit(byte[] data, long offsetBit, long length) {
        long retLength;
        if (length > bitLength()) {
            length = bitLength();
        }
        retLength = length;
        int of = (int) (offsetBit / 8);
        int ofbit = (int) (offsetBit % 8);

        if (ofbit > 0 && length >= (8 - ofbit)) {
            data[of] &= 0xff - andMask(8-ofbit);
            data[of] |= (byte) readInt(8 - ofbit);
            of++;
            length -= 8 - ofbit;
            ofbit = 0;
        }

        int v;
        while (length >= 24) {
            v = readInt(24);
            data[of] = (byte) (v >> 16);
            data[of + 1] = (byte) ((v >> 8) & 0xff);
            data[of + 2] = (byte) (v & 0xff);
            of += 3;
            length -= 24;
        }
        while (length >= 8) {
            data[of] = (byte) readInt(8);
            of++;
            length -= 8;
        }
        if (length > 0) {
            data[of] &= andMask((int) (8-length));
            data[of] |= (byte) (readInt((int) length) << (8 - length));
        }
        return retLength;
    }

    /**
     * ?
     * @param data
     * @param offsetBit
     * @param length
     * @return
     */
    @Override
    public long backReadBit(byte[] data, long offsetBit, long length) {
        long retLength;
        if (length > bitLength()) {
            length = bitLength();
        }
        retLength = length;
        int of = (int) ((offsetBit + length) / 8);
        int ofbit = (int) ((offsetBit + length) % 8);

        if ( ofbit > 0 && length > ofbit) {
            data[of] &= 0xff - andMask(ofbit);
            data[of] |= backReadInt((int)ofbit);
            length -= ofbit;
        }
        
        int v;
        while (length >= 24) {
            v = backReadInt(24);
            of -= 3;
            data[of] = (byte) (v & 0xff);
            data[of + 1] = (byte) ((v >> 8) & 0xff);
            data[of + 2] = (byte) (v >> 16);
            length -= 24;
        }
        while (length >= 8) {
            of--;
            data[of] = (byte) backReadInt(8);
            length -= 8;
        }

        if (ofbit > 0 && length >= (8 - ofbit)) { //todo: ofbit
            of--;
            data[of] &= andMask(ofbit);
            data[of] |= (byte)(backReadInt(8 - ofbit) << ofbit);
            length -= 8 - ofbit;
            ofbit = 0;
        }
        return retLength;
    }

    /**
     * 簡単な int, long
     *
     * @param data
     * @param bitLength
     */
    @Override
    public void writeBit(int data, int bitLength) {
        data &= andMask(bitLength);
        int oldData = 0;
        if (writePadding > 0) {
            oldData = pac.backRead();
            if (bitLength >= writePadding) { // | oooo nnnn |
                int d = oldData | (data >>> (bitLength - writePadding));
                pac.write(d);

                bitLength -= writePadding;
                writePadding = 0;
                data &= andMask(bitLength);
            } else {
                int d = oldData | (data << writePadding - bitLength);
                pac.write(d);

                writePadding -= bitLength;
                return;
            }
        }

        while (writePadding == 0 && bitLength >= 8) {
            pac.write(data >>> (bitLength - 8));

            bitLength -= 8;
            data &= andMask(bitLength);
        }
        if (bitLength > 0) {
            writePadding = 8 - bitLength;
            pac.write(data << writePadding);
        }
    }

    /**
     * 簡単な int, long
     *
     * @param data
     * @param bitLength
     */
    public void backWriteBit(int data, int bitLength) {
        data &= andMask(bitLength);
        int oldData = 0;
        if (readPadding > 0) {
            oldData = pac.read();
            int d = oldData | (data << (8 - readPadding)); // ?
            pac.backWrite(d);
            if (bitLength >= readPadding) { // | nnnn oooo |
                bitLength -= readPadding;
                data >>>= readPadding;
                readPadding = 0;
                //data &= andMask(bitLength);
            } else {
                readPadding -= bitLength;
                bitLength = 0;
                return;
            }
        }

        while (bitLength >= 8) {
            pac.backWrite(data & 0xff);
            bitLength -= 8;
            data >>>= 8;
        }
        if (bitLength > 0) {
            // lenは1-7, writePadding は 0-7, bitLength < writePadding
            readPadding = 8 - bitLength;
            pac.backWrite(data & andMask(bitLength));
            // data = 0;
        }
    }

    /**
     * 簡単な int, long
     *
     * @param data
     * @param offsetBit
     * @param bitLength
     */
    @Override
    public void writeBit(byte[] data, long offsetBit, long bitLength) {
        int of = (int) (offsetBit / 8);
        offsetBit %= 8;

        while (offsetBit + bitLength >= 32) {
            writeBit(((data[of] & 0xff) << 24) | ((data[of + 1] & 0xff) << 16) | ((data[of + 2] & 0xff) << 8) | (data[of + 3] & 0xff), 32 - (int)offsetBit);
            of += 4;
            bitLength -= 32 - offsetBit;
            offsetBit = 0;
        }

        while (offsetBit + bitLength >= 16) {
            writeBit(((data[of] & 0xff) << 8) | (data[of + 1] & 0xff), 16 - (int)offsetBit);
            of += 2;
            bitLength -= 16 - offsetBit;
            offsetBit = 0;
        }

        while (offsetBit + bitLength >= 8) {
            writeBit(data[of] & 0xff, 8 - (int)offsetBit);
            of++;
            bitLength -= 8 - offsetBit;
            offsetBit = 0;
        }

        // |xx12xxxx|
        if (bitLength > 0) {
            int d = data[of] & 0xff;
            writeBit(d >>> (8 - offsetBit - bitLength), (int) bitLength);
        }
    }

    /**
     * 
     * @param i
     * @param i0 
     */
    public void backWrite(int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int[] b = new int[32];
        int len = 32;
        if (bitLength() < 32) {
            len = (int) bitLength();
        }
        sb.append(len);
        sb.append(":");
        for (int i = 0; i < len; i++) {
            b[i] = this.readInt(1);
            sb.append(b[i]);
        }
        for (int i = 0; i < len; i++) {
            this.backWriteBit(b[len - 1 - i], 1);
        }

        return sb.toString();
    }

    @Override
    public void backWriteBit(byte[] data, long offsetBit, long bitLength) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream getInputStream() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] toByteArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void backWrite(int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void backWrite(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int backRead() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int backRead(byte[] data, int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int backRead(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
