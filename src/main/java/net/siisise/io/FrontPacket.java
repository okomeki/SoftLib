package net.siisise.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Packet と InputStream の共通のものにしたい
 */
public interface FrontPacket {
    
    /**
     * ストリームと完全互換ではないがそれっぽくしてくれる.
     * @return 
     */
    InputStream getInputStream();
    /**
     * 書き戻す用.
     * Packetの頭にデータが増える.
     * 頭に書き足す/戻すので繋ぎ方に注意.
     * @return 特殊用途OutputStream
     */
    OutputStream getBackOutputStream();

    int read();
    int read(byte[] data, int offset, int length);
    int read(byte[] data);

    byte[] toByteArray();

    /**
     * 読み出しの手前に1バイト戻す.
     * @param data 下位8ビットのみ
     */
    void backWrite(int data);
    /**
     * 読み出しの手前にデータを足す.
     * 読んだデータを戻すのに便利.
     * @param data データを含む配列.
     * @param offset 開始位置
     * @param length データ長
     */
    void backWrite(byte[] data, int offset, int length);
    void backWrite(byte[] data);

    long length();
    int size();
}
