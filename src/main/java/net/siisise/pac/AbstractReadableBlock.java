/*
 * Copyright 2022 okome.
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
package net.siisise.pac;

import java.io.InputStream;
import net.siisise.io.FrontPacket;
import net.siisise.io.InputInputStream;
import net.siisise.io.PacketA;
import net.siisise.io.RevInputStream;

/**
 * 一般的なところだけ載せる.
 */
public abstract class AbstractReadableBlock implements ReadableBlock {

    @Override
    public InputStream getInputStream() {
        return new InputInputStream(this) {
            int mark = -1;

            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public void mark(int readlimit) {
                mark = AbstractReadableBlock.this.backSize();
            }

            @Override
            public void reset() {
                if (mark >= 0) {
                    seek(mark);
                }
            }
        };
    }

    @Override
    public int read() {
        byte[] d = new byte[1];
        int size = read(d, 0, 1);
        return ( size < 0 ) ? -1 : d[0] & 0xff;
    }

    @Override
    public int read(byte[] d) {
        return read(d,0, d.length);
    }

    @Override
    public byte[] toByteArray() {
        byte[] tmp = new byte[size()];
        read(tmp);
        return tmp;
    }

    /**
     * 仮
     * @param length
     * @return 
     */
    @Override
    public FrontPacket split(int length) {
        length = Integer.min(length, size());
        byte[] tmp = new byte[length];
        int size = read(tmp);
        PacketA pac = new PacketA();
        if ( size == length ) {
            pac.dwrite(tmp);
        } else {
            pac.write(tmp,0,size);
        }
        return pac;
    }

    @Override
    public RevInputStream getBackInputStream() {
        return new RevInputStream(this);
    }

    @Override
    public int backRead() {
        byte[] d = new byte[1];
        int size = backRead(d,0,1);
        return ( size < 0 ) ? -1 : d[0] & 0xff;
    }

    @Override
    public int backRead(byte[] data) {
        return backRead(data, 0, data.length);
    }
}
