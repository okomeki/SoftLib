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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PacketのふりをするStream.
 * Packet を InputStream の頭につけたい。
 * Streamとして振る舞うのがメイン。
 * 使い終わったStreamは閉じる.
 */
public class StreamFrontPacket implements FrontPacket {

    private final FrontPacket inpac = new PacketA();

    private final StreamFrontInputStream in;

    /**
     *
     * @param in 入力
     */
    public StreamFrontPacket(InputStream in) {
        this.in = new StreamFrontInputStream(in);
    }

    public StreamFrontPacket(Reader reader) {
        this(new ReaderInputStream(reader, 30));
    }

    @Override
    public byte get() {
        byte[] d = new byte[1];
        get(d,0,1);
        return d[0];
    }

    @Override
    public long get(byte[] d, int offset, int length) {
        if ( size() < length ) {
            throw new java.nio.BufferOverflowException();
        }
        read(d,offset,length);
        return length;
    }

    private class StreamFrontInputStream extends InputStream {

        private InputStream in;

        StreamFrontInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            if (inpac.size() > 0) {
                return inpac.read();
            }
            if (in == null) { // streamが終わるとPacketとして振る舞うのでExceptionは吐かない.
                throw new IOException();
//                return -1;
            }

            int v = in.read();
            if (v < 0) {
                in.close();
                in = null;
            }
            return v;
        }

        @Override
        public int read(byte[] d) throws IOException {
            return read(d, 0, d.length);
        }

        @Override
        public int read(byte[] data, int offset, int length) throws IOException {
            int len = inpac.read(data, offset, length);
            if (len < length && in != null) { // Streamに頼る.
                if (len >= 0) {
                    offset += len;
                    length -= len;
                }
                int l2 = in.read(data, offset, length);
                if (l2 < 0) {
                    in.close();
                    in = null;
                    if (len == 0) {
                        len = -1;
                    }
                } else {
                    return len + l2;
                }
            }
            return len;
        }
        
        @Override
        public long skip(long length) {
            long min = inpac.skip(length);
            try {
                return min + in.skip(length - min);
            } catch (IOException ex) {
            }
            return min;
        }

        @Override
        public int available() throws IOException {
            if (in == null) {
                return inpac.size();
            }
            return inpac.size() + in.available();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    /**
     * @return
     */
    @Override
    public InputStream getInputStream() {
        return new FilterInput(this);
//        return in;
    }

    @Override
    public OutputStream getBackOutputStream() {
        return inpac.getBackOutputStream();
    }

    @Override
    public int read() {
        try {
            if (in.available() > 0) {
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
            while (in.available() > 0 && length > 0) {
                int l = in.read(data, offset, length);
                if (l >= 0) {
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
    public long skip(long size) {
        return in.skip(size);
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
    public void backWrite(byte[] data) {
        inpac.backWrite(data);
    }

    @Override
    public void backWrite(byte[] data, int offset, int length) {
        inpac.backWrite(data, offset, length);
    }

    @Override
    public long backWrite(RevInput rin) {
        return inpac.backWrite(rin);
    }

    @Override
    public long backWrite(RevInput rin, long length) {
        return inpac.backWrite(rin, length);
    }

    @Override
    public void dbackWrite(byte[] data) {
        inpac.dbackWrite(data);
    }

    @Override
    public void flush() {
        inpac.flush();
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
            throw new java.lang.IllegalStateException(ex);
        }
    }

    @Override
    public long length() {
        return size();
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

    @Override
    public Packet readPacket(long length) {
        PacketA pp = new PacketA();
        length -= pp.write(inpac, length);
        pp.write(this,length);
        return pp;
    }

    /**
     * for Debug.
     * でばっぐ用なのでてきとう
     *
     * @return
     */
    @Override
    public String toString() {
        return "StreamFrontPacket size:" + size();
    }
}
