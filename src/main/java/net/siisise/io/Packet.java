package net.siisise.io;

/**
 * First In First Out Stream Packet.
 * 配列のような固定長でもなく、リストのような細切れでもないものを作ってみた。
 * メモリとストレージの中間を狙ったようなそうでもないような Bufferの可変長。
 * PipedInputStream / PipedOutputStream のような延長線。
 * 逆向きの読み書きが可能。中間の読み書きは使わないのでやめた。
 * 
 * @since JDK 1.1
 */
public interface Packet extends FrontPacket,BackPacket {

    /**
     * 32ビットでは足りないかもと足してみた
     * @return 
     */
    @Override
    long length();
    /**
     * 32ビット内であればそのサイズ、それ以上はIntegerの最大値
     * @return 
     */
    @Override
    int size();
}
