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

/**
 * Input を InputStream に変える.
 */
public class FilterInput extends AbstractInput {

    private final Input in;

    public FilterInput(Input pac) {
        this.in = pac;
    }

    @Override
    public int read() {
        return in.read();
    }
    
    @Override
    public int read(byte[] dst, int offset, int length) {
        return in.read(dst, offset, length);
    }
    
    @Override
    public long length() {
        return in.length();
    }

    @Override
    public Packet split(long length) {
        return in.split(length);
    }

    @Override
    public long skip(long size) {
        return in.skip(size);
    }
}