package net.siisise.io;

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

    @Override
    public long bitLength() {
        return pac.length() * 8 - readPadding - writePadding;
    }

    /**
     *
     * @param len
     * @return
     */
    final int andMask(int len) {
        //if ( len == 32 ) return -1;
        return (int) (1l << len) - 1;
    }

    @Override
    public int read() {
        if (bitLength() < 8) {
            return -1;
        }
        return readInt(8);
    }

    /**
     * 8ビット単位とも限らないかもしれない
     * @param data
     */
    @Override
    public int read(byte[] data) {
        long len = readBit(data, 0, data.length * 8);
        int a = (int) (len % 8);
        if ( a > 0 ) {
            backWriteBit(data, len - a, a);
        }
        return (int) (len / 8);
    }
    
    @Override
    public int read(byte[] data, int offset, int length) {
        long len = readBit(data, offset * 8, length * 8l);
        int a = (int) (len % 8);
        if ( a > 0 ) {
            backWriteBit(data, len - a, a);
        }
        return (int) (len / 8);
    }

    /**
     *
     * @param data
     */
    @Override
    public void write(byte[] data) {
        writeBit(data, 0, data.length * 8);
    }

    /**
     *
     * @param data
     * @param offset
     * @param length
     */
    @Override
    public void write(byte[] data, int offset, int length) {
        writeBit(data, offset * 8, length * 8);
    }

    @Override
    public void writeBit(BitPacket pac) {
        writeBit(pac, pac.bitLength());
    }

    @Override
    public void writeBit(BitPacket bp, long bitLength) {
        byte[] data = new byte[(int) (bitLength + 7) / 8];
        bp.readBit(data, 0, bitLength);
        writeBit(data, 0, bitLength);
    }
}
