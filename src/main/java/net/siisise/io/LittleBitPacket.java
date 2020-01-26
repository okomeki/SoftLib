package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * little endian か?
 * 最下位ビットが先頭 |76543210|fedcba98|
 * byte列は左詰め?、intは右詰め
 * @deprecated まだ
 * @author okome
 */
public class LittleBitPacket extends BaseBitPac {

    @Override
    public int backReadInt(int bit) {
        if (bit > bitLength()) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        int ret = 0;
        int flen = 8 - writePadding;
        if (bit >= flen) { // そのまま
            ret = (int) (pac.backRead() & andMask(flen));
            bit -= flen;
            writePadding = 0;
        }
        int len = (int) bit / 8;
        if (len > 0) {
            byte[] d = new byte[len];
            pac.backRead(d);
            for (byte x : d) {
                ret <<= 8;
                ret |= x & 0xff;
            }
            bit -= len * 8;
        }
        if (bit > 0) { // bit = 1～7 ビット構造で異なる
            int d = pac.backRead();
            ret <<= bit;
            writePadding += bit;
            ret |= d >>> (8 - writePadding);
            if (pac.length() == 0 && writePadding + readPadding == 8) {
                readPadding = 0;
                writePadding = 0;
            } else {
                d &= andMask(8 - writePadding);
                pac.write(d);
            }
        }
        return ret;
    }
    
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
            ret = (int) ((pac.read() >> readPadding) & andMask(flen));
            bit -= flen;
            readPadding = 0;
        }
        int len = (int) bit / 8;
        if (len > 0) {
            byte[] d = new byte[len];
            pac.read(d);
            for (byte x : d) {
                ret |= (x & 0xff) << flen;
                flen += 8;
            }
            bit -= len * 8;
        }
        if (bit > 0) { // bit = 1～7 ビット構造で異なる
            int d = ((pac.read() >> readPadding) & andMask(bit)) << flen;
            readPadding += bit;
            ret |= d;
            if (pac.length() == 0 && readPadding + writePadding == 8) {
                readPadding = 0;
                writePadding = 0;
            } else {
                d &= andMask(8 - readPadding) << bit;
                pac.backWrite(d);
            }
        }
        return ret;
    }

    @Override
    public LittleBitPacket readPac(int bit) {
        if (bit > bitLength()) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        LittleBitPacket bp = new LittleBitPacket();
        if (bit >= (8 - readPadding)) { // そのまま
            //    bp.pac.write(d);
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
        int len = bit / 8;
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
     * 右詰め |76543210|fedcba98|
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
            data[of] &= andMask(ofbit);
            data[of] |= (byte)(readInt(8 - ofbit) << ofbit);
            of++;
            length -= 8 - ofbit;
            ofbit = 0;
        }
        
        int v;
        while (length >= 24) {
            v = readInt(24);
            data[of] = (byte) (v & 0xff);
            data[of + 1] = (byte) ((v >> 8) & 0xff);
            data[of + 2] = (byte) (v >> 16);
            of += 3;
            length -= 24;
        }
        while (length >= 8) {
            data[of] = (byte) readInt(8);
            of++;
            length -= 8;
        }
        
        if (length > 0) {
            data[of] &= andMask(ofbit) | (0xff - andMask((int) length+ofbit));
            data[of] |= readInt((int)length) << ofbit;
        }
        return retLength;
    }

    /**
     * ぬ?
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

        if (ofbit > 0 && length >= ofbit) { // まだ
            data[of] &= andMask(8 - ofbit);
            data[of] |= (byte) backReadInt(ofbit);
            of++;
            length -= 8 - ofbit;
            ofbit = 0;
        }
        // 
        throw new java.lang.UnsupportedOperationException();
/*
        int v;
        while (length >= 24) {
            v = backReadInt(24);
            data[of] = (byte) (v >> 16);
            data[of + 1] = (byte) ((v >> 8) & 0xff);
            data[of + 2] = (byte) (v & 0xff);
            of += 3;
            length -= 24;
        }
        while (length >= 8) {
            data[of] = (byte) backReadInt(8);
            of++;
            length -= 8;
        }
        if (length > 0) {
            data[of] &= andMask((int) (8-length));
            data[of] |= (byte) (backReadInt((int) length) << (8 - length));
        }
        return retLength;
*/
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
            int d = oldData | (data << (8 - writePadding));
            pac.write(d & 0xff);
            if (bitLength >= writePadding) { // | nnnn oooo |
                bitLength -= writePadding;
                data >>>= writePadding;
                writePadding = 0;
                //data &= andMask(bitLength);
            } else {
                writePadding -= bitLength;
                return;
            }
        }

        while (bitLength >= 8) {
            pac.write(data & 0xff);
            bitLength -= 8;
            data >>>= 8;
        }
        if (bitLength > 0) {
            writePadding = 8 - bitLength;
            pac.write(data & andMask(bitLength));
        }
    }

    /**
     * 簡単な int, long
     * |76543210|fedbca98|
     * 
     * @param data
     * @param bitLength
     */
    public void backWriteBit(int data, int bitLength) {
        data &= andMask(bitLength);
        int oldData = 0;
        if (readPadding > 0) {
            oldData = pac.read();
            if (bitLength >= readPadding) { // | oooo nnnn |
                int d = oldData | (data >>> (bitLength - readPadding));
                pac.backWrite(d);
                
                bitLength -= readPadding;
                readPadding = 0;
                data &= andMask(bitLength);
            } else {
                int d = oldData | (data << readPadding - bitLength);
                pac.backWrite(d);
                
                readPadding -= bitLength;
                return;
            }
        }
        
        while (readPadding == 0 && bitLength >= 8) {
            pac.backWrite(data >>> (bitLength - 8));
            
            bitLength -= 8;
            data &= andMask(bitLength);
        }
        if (bitLength > 0) {
            readPadding = 8 - bitLength;
            pac.write(data << readPadding);
        }
    }

    /**
     * 簡単な int, long
     * 
     * @param data
     * @param offset
     * @param length
     */
    @Override
    public void writeBit(byte[] data, long offset, long length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void backWriteBit(byte[] data, long offsetBit, long bitLength) {
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
    public void write(int data) {
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
