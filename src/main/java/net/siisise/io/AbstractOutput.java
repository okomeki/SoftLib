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

import java.io.OutputStream;

/**
 *
 */
public abstract class AbstractOutput extends OutputStream implements Output {

    @Override
    public OutputStream getOutputStream() {
        return this;
    }

    @Override
    public void write(int d) {
        write(new byte[] {(byte)d});
    }

    @Override
    public void write(byte[] d) {
        write(d,0,d.length);
    }
    
    @Override
    public abstract void write(byte[] d, int offset, int length);

    @Override
    public void dwrite(byte[] d) {
        write(d);
    }

    @Override
    public void write(Input pac) {
        Output.write(this, pac, pac.length());
    }

    @Override
    public Output put(byte data) {
        return put(new byte[] {data});
    }

    @Override
    public Output put(byte[] data) {
        return put(data,0,data.length);
    }
}
