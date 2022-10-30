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
package net.siisise.io;

import java.io.InputStream;

/**
 *
 */
public class InputInputStream extends InputStream implements Input {

    private final Input in;

    public InputInputStream(Input pac) {
        this.in = pac;
    }

    @Override
    public int read() {
        return in.read();
    }
    
    @Override
    public int read(byte[] d) {
        return in.read(d,0,d.length);
    }

    @Override
    public int read(byte[] dst, int offset, int length) {
        return in.read(dst, offset, length);
    }
    
    @Override
    public byte[] toByteArray() {
        return in.toByteArray();
    }

    @Override
    public int available() {
        return in.size();
    }

    @Override
    public InputStream getInputStream() {
        return this;
    }

    @Override
    public long length() {
        return in.length();
    }

    @Override
    public int size() {
        return in.size();
    }

    @Override
    public FrontPacket split(int length) {
        return in.split(length);
    }

}
