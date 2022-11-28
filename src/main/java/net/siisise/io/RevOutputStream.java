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

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public class RevOutputStream extends OutputStream implements RevOutput {
    
    final RevOutput out;

    public RevOutputStream(RevOutput o) {
        out = o;
    }
    
    @Override
    public void write(int b) throws IOException {
        out.backWrite(b);
    }
    
    @Override
    public void write(byte[] d) {
        out.backWrite(d, 0, d.length);
    }
    
    @Override
    public void write(byte[] d, int offset, int length) {
        out.backWrite(d, offset, length);
    }

    @Override
    public RevOutputStream getBackOutputStream() {
        return this;
    }

    @Override
    public void backWrite(int d) {
        out.backWrite(d);
    }

    @Override
    public void backWrite(byte[] d, int offset, int length) {
        out.backWrite(d, offset, length);
    }

    @Override
    public void backWrite(byte[] d) {
        out.backWrite(d);
    }

    /**
     * 複製しない(可能な場合)
     * @param d データ列
     */
    @Override
    public void dbackWrite(byte[] d) {
        out.dbackWrite(d);
    }
    
    @Override
    public void flush() {
        out.flush();
    }

    @Override
    public long backWrite(RevInput rin) {
        return out.backWrite(rin);
    }

    @Override
    public long backWrite(RevInput rin, long length) {
        return out.backWrite(rin, length);
    }
}
