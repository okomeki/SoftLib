package net.siisise.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PacketのふりをするStream
 * Packet を InputStream の頭につけたい。
 * Streamとして振る舞うのがメイン。
 */
public class StreamFrontPacket implements FrontPacket {

    private final FrontPacket inpac = new PacketA();

    private final FrontInputStream in;

    public StreamFrontPacket(InputStream in) {
        this.in = new FrontInputStream(in);
    }

    public StreamFrontPacket(Reader reader) {
        this(new ReaderInputStream(reader, 30));
    }

    @Override
    public long length() {
        return size();
    }

    class FrontInputStream extends java.io.FilterInputStream {

        // タイミングを逃したEOF
        boolean eof = false;

        FrontInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            if ( inpac.size() > 0 ) {
                return inpac.read();
            }
            if ( eof ) {
                eof = false;
                return -1;
            }
//            if ( fin.available() > 0 ) {  // ToDo: いらない?
            return in.read();
//            }
//            return -1; // fin.read();
        }

        @Override
        public int read(byte[] data, int offset, int length) throws IOException {
            int len = inpac.read(data, offset, length);
            if ( len >= 0 && len < length) {
                offset += len;
                length -= len;
                if ( eof ) {
                    eof = false;
                    return -1;
                }
                int l2 = in.read(data,offset,length);
                if ( l2 >= 0 ) {
                    return len + l2;
                } else {
                    eof = true;
                }
            }
            return len;
        }

        @Override
        public int available() throws IOException {
            return inpac.size() + in.available();
        }
    }

    /**
     * @return
     */
    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public OutputStream getBackOutputStream() {
        return inpac.getBackOutputStream();
    }

    @Override
    public int read() {
        try {
            if ( in.available() > 0 ) {
                return in.read();
            } else {
                return -1;
            }
        } catch (IOException ex) {
            Logger.getLogger(StreamFrontPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int read(byte[] data, int offset, int length) {
        try {
            int len = 0;
            while ( in.available() > 0 && length > 0 ) {
                int l = in.read(data,offset,length);
                if ( l >= 0 ) {
                    len += l;
                    offset += l;
                    length -= l;
                } else {
                    break;
                }
            }
            return len;

        } catch (IOException ex) {
            Logger.getLogger(StreamFrontPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public int read(byte[] data) {
        return read(data, 0, data.length);
    }

    @Override
    public byte[] toByteArray() {
        try {
            return FileIO.binRead(in);
        } catch (IOException ex) {
            Logger.getLogger(StreamFrontPacket.class.getName()).log(Level.SEVERE, null, ex);
            throw new UnsupportedOperationException(ex);
        }
    }

    @Override
    public void backWrite(int data) {
        inpac.backWrite(data);
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        inpac.backWrite(data,offset,length);
    }

    @Override
    public void backWrite(byte[] data) {
        inpac.backWrite(data);
    }

    @Override
    public void dbackWrite(byte[] data) {
        inpac.dbackWrite(data);
    }

    /**
     * サイズ取得。
     * availableしか使えないので不確定な要素.
     *
     * @return
     */
    @Override
    public int size() {
        try {
            return in.available();
        } catch (IOException ex) {
            Logger.getLogger(StreamFrontPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * 連結した入力を閉じる.
     * Packetには特殊な要素.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        in.close();
    }

}
