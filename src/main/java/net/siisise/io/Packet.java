package net.siisise.io;

import java.io.OutputStream;

/**
 * First In First Out Stream Packet.
 * 配列のような固定長でもなく、リストのような細切れでもないものを作ってみた。
 * メモリとストレージの中間を狙ったようなそうでもないような Bufferの可変長。
 * PipedInputStream / PipedOutputStream のような延長線。
 * 逆向きの読み書きが可能。中間の読み書きは使わないのでやめた。
 * 
 * @since JDK 1.1
 */
public interface Packet extends FrontPacket {

    OutputStream getOutputStream();
    
    void write(int b);
    void write(byte[] b, int offset, int length);
    void write(byte[] b);
    
    int backRead();
    int backRead(byte[] b, int offset, int length);
    int backRead(byte[] b);
    
    /**
     * 32ビットでは足りないかもと足してみた
     * @return 
     */
    long length();
    int size();
}
