/*
 * Copyright 2020-2024 okome.
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
 * little endian か?
 * 最下位ビットが先頭 |76543210|fedcba98|
 * byte列は左詰め?、intは右詰め
 *
 * @deprecated テストはしていないかもしれない
 */
@Deprecated
public class LittleBitPacket extends BaseBitPac {

    class LittleBitInputStream extends BitInputStream {

        /**
         * ビット読み込み.
         * バイト順では下位バイトが先頭
         *
         * @param bit 0 から 32ぐらい 32を超える場合は先頭のみ返され残りはskipされる形
         * @return |xxdcba98|76543210|
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
        public LittleBitPacket readBitPacket(long bit) {
            if (bit > bitLength()) {
                throw new java.lang.IndexOutOfBoundsException();
            }
            LittleBitPacket bp = new LittleBitPacket();
            if (bit + readPadding < 8) {
                int d = readInt((int)bit);
                bp.writeBit(d, (int)bit);
                return bp;
            } // そのまま
            
            //先頭Padding処理
            //    bp.block.write(d);
            if (readPadding > 0) {
                int c = pac.read();
                bp.pac.write(c);
                bp.readPadding = readPadding;
                bit -= (8 - readPadding);
                readPadding = 0;
            }
            
            int len = (int)(bit / 8);
            if (len > 0) {
                byte[] d = new byte[len];
                pac.read(d);
                bp.pac.write(d);
                bit -= len * 8;
            }
            if (bit > 0) { // bit = 1～7 ビット構造で異なる
                int d = readInt((int)bit);
                bp.writeBit(d, (int)bit);
            }
            return bp;
        }

        /**
         * readのビット版 入れ物はbyte列
         * 右詰め |76543210|fedcba98|
         *
         * @param data 戻りデータ
         * @param offsetBit 開始ビット位置
         * @param length ビット長
         * @return 読み込んだビット長
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

            if (ofbit > readPadding) {
                //    throw new UnsupportedOperationException();
            } else if (ofbit == readPadding) {
                byte[] sd = new byte[(int) ((length + readPadding + 7) / 8)];
                pac.read(sd);
                sd[0] |= (byte) (data[of] & (0xff - andMask(8 - ofbit)));
                System.arraycopy(sd, 0, data, of, (int) ((length + readPadding) / 8));
                long l = ((length + readPadding) / 8) * 8;
                length -= l - readPadding;
                offsetBit += l;
                of = (int) (offsetBit / 8);
                ofbit = (int) (offsetBit % 8);
                readPadding = 0;
            } else {
                //    throw new UnsupportedOperationException();
            }

            if (ofbit > 0 && length >= (8 - ofbit)) {
                data[of] &= andMask(ofbit);
                data[of] |= (byte) (readInt(8 - ofbit) << ofbit);
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
                data[of] &= andMask(ofbit) | (0xff - andMask((int) length + ofbit));
                data[of] |= readInt((int) length) << ofbit;
            }
            return retLength;
        }
    }

    /**
     * 逆.
     */
    class BackLittleBitInputStream extends BitInputStream {

        /**
         * 逆ビット読み込み.
         * 書き込んだ方向から読み込む
         *
         * @param bit 0 から 32ぐらい
         * @return
         */
        @Override
        public int readInt(int bit) {
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
            int len = bit / 8;
            if (len > 0) {
                byte[] d = new byte[len];
                pac.backRead(d);
                for (int i = d.length - 1; i >= 0; i--) {
                    ret <<= 8;
                    ret |= d[i] & 0xff;
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
         * Little Endian が成立する形で背面から読み込み.
         *
         * @param data 読み込み先
         * @param offsetBit data bit位置
         * @param length bitサイズ
         * @return 読み込まれたbitサイズ
         */
        @Override
        public long readBit(byte[] data, final long offsetBit, long length) {
            long l = bitLength();
            if (length > l) {
                length = l;
            }
            long retLength = length;
            int of = (int) ((offsetBit + length) / 8);
            int ofbit = (int) ((offsetBit + length) % 8); // あまりビット

            if (ofbit > 0 && length >= ofbit) { // 末尾は下位ビットを埋める
                data[of] &= andMask(8 - ofbit);
                data[of] |= (byte) readInt(ofbit) << (8 - ofbit);
                length -= ofbit;
                ofbit = 0;
            }

            int v;
            while (length >= 24) {
                v = readInt(24);
                of -= 3;
                data[of] = (byte) (v & 0xff);
                data[of + 1] = (byte) ((v >> 8) & 0xff);
                data[of + 2] = (byte) (v >> 16);
                length -= 24;
            }
            while (length >= 8) {
                of--;
                data[of] = (byte) readInt(8);
                length -= 8;
            }
            if (length > 0) { //　メモ 下位ビットから埋める // 先頭は上ビットを埋める
                if (ofbit == 0) {
                    of--;
                    ofbit = 8;
                }
                int n = ofbit - (int) length;
                data[of] &= 0xff ^ (andMask((int) length) << n);
                data[of] |= (byte) (readInt((int) length) << n);
            }

            return retLength;
        }

        /**
         * ビット単位で分割.
         * ToDo: padding考慮して分割するともう少し速い.
         * 
         * @param bitLength ビット長
         * @return 
         */
        @Override
        public LittleBitPacket readBitPacket(long bitLength) {
            LittleBitPacket p = new LittleBitPacket();
            if ( bitLength < 8) {
                int c = readInt((int)bitLength);
                p.writeBit(c, (int)bitLength);
                return p;
            }
            //先頭Padding処理
            //    bp.block.write(d);
            if (readPadding > 0) {
                int c = pac.read();
                p.pac.write(c);
                p.readPadding = readPadding;
                bitLength -= (8 - readPadding);
                readPadding = 0;
            }
            
            byte[] tmp = new byte[(int)((bitLength + 7) / 8)];
            readBit(tmp, 0, bitLength);
            p.writeBit(tmp, 0, bitLength);
            return p;
        }
    }

    class LittleBitOutputStream extends BitOutputStream {

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

            if (bitLength >= 8) {
                byte[] b = new byte[bitLength / 8];
                int i = 0;
                while (bitLength >= 8) {
                    b[i++] = (byte) (data & 0xff);
                    bitLength -= 8;
                    data >>>= 8;
                }
                pac.write(b);
            }
            if (bitLength > 0) {
                writePadding = 8 - bitLength;
                pac.write(data & andMask(bitLength));
            }
        }

        /**
         * 書き
         *
         * @param data データ含む列
         * @param bitOffset ビット位置
         * @param bitLength ビット長
         */
        @Override
        public void writeBit(byte[] data, long bitOffset, long bitLength) {
            int of = (int) (bitOffset / 8);
            int bit = (int) (bitOffset % 8);

            /*
            if (bit > 0 && bitLength >= (8 - bit)) {
                int d = data[of] & 0xff;
                writeBit(d >>> bit, 8 - bit);
                of++;
                bitLength -= bit;
                bit = 0;
            }
             */
            while (bit + bitLength >= 32) {
                writeBit((data[of] & 0xff) | ((data[of + 1] & 0xff) << 8) | ((data[of + 2] & 0xff) << 16) | ((data[of + 3] & 0xff) << 24), 32 - bit);
                of += 4;
                bitLength -= 32 - bit;
                bit = 0;
            }

            while (bit + bitLength >= 16) {
                writeBit((data[of] & 0xff) | ((data[of + 1] & 0xff) << 8), 16 - bit);
                of += 2;
                bitLength -= 16 - bit;
                bit = 0;
            }

            while (bit + bitLength >= 8) {
                writeBit(data[of] & 0xff, 8 - bit);
                of += 1;
                bitLength -= 8 - bit;
                bit = 0;
            }

            if (bitLength > 0) {
                int d = data[of] & 0xff;
                writeBit(d >>> bit, (int) bitLength);
            }
        }
    }

    /**
     * 逆書き込み.
     */
    class BackLittleBitOutputStream extends BitOutputStream {

        /**
         * 簡単な int, long
         * |76543210|fedbca98|
         *
         * @param data データ
         * @param bitLength 書き込みビット長
         */
        @Override
        public void writeBit(int data, int bitLength) {
            data &= andMask(bitLength);
            int oldData;
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
         * backWriteBit
         *
         * @param data data
         * @param bitOffset ビット位置
         * @param bitLength ビット長
         */
        @Override
        public void writeBit(byte[] data, long bitOffset, long bitLength) {
            int of = (int) ((bitOffset + bitLength) / 8);
//            long retLength = bitLength;
            int ofbit = (int) ((bitOffset + bitLength) % 8); // あまりビット

            if (ofbit > 0 && bitLength >= ofbit) { // 末尾は下位ビットを埋める
                writeBit((data[of] & 0xff), ofbit);
                bitLength -= ofbit;
                ofbit = 0;
            }

            int v;
            while (bitLength >= 24) {
                of -= 3;
                v = ( data[of]     & 0xff       )
                  | ((data[of + 1] & 0xff) <<  8)
                  | ((data[of + 2] & 0xff) << 16);
                writeBit(v, 24);
                bitLength -= 24;
            }
            while (bitLength >= 8) {
                of -= 1;
                writeBit(data[of] & 0xff, 8);
                bitLength -= 8;
            }
            if (bitLength > 0) { //　メモ 下位ビットから埋める // 先頭は上ビットを埋める
                if (ofbit == 0) {
                    of--;
                    ofbit = 8;
                }
                int n = ofbit - (int) bitLength;
                writeBit(data[of] >> n, (int) bitLength);
            }
        }
    }

    public LittleBitPacket() {
        in = new LittleBitInputStream();
        backIn = new BackLittleBitInputStream();
        out = new LittleBitOutputStream();
        backOut = new BackLittleBitOutputStream();
    }

    @Override
    public LittleBitPacket readPacket(long length) {
        LittleBitPacket lp = new LittleBitPacket();
        Output.write(lp, this, length);
        return lp;
    }

    @Override
    public LittleBitPacket backReadPacket(long length) {
        LittleBitPacket bp = new LittleBitPacket();
        RevOutput.backWrite(bp, this, length);
        return bp;
    }
}
