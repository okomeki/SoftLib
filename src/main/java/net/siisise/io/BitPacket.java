package net.siisise.io;

/**
 * javaのBitSetと互換かなにかにしたい
 *
 * @author okome
 */
public interface BitPacket extends FrontPacket,BackPacket {

    int readInt(int bitLength);
    int backReadInt(int bitLength);
    long readBit(byte[] data, long offsetBit, long bitLength);
    long backReadBit(byte[] data, long offsetBit, long bitLength);

    BitPacket readPac(int length);

    void writeBit(int data, int bitLength);
    void writeBit(byte[] data, long offsetBit, long bitLength);
    void backWriteBit(byte[] data, long offsetBit, long bitLength);

    void writeBit(BitPacket pac, long bitLength);
    void writeBit(BitPacket pac);

    long bitLength();
}
