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
public class RevInputStream extends InputStream implements RevInput {
    
    final RevInput in;
    
    public RevInputStream(RevInput in) {
        this.in = in;
    }

    /**
     * 逆に繋げるかもしれないので仮
     * @return 
     */
    @Override
    public int read() {
        return in.backRead();
    }
    
    /**
     *
     * @param d
     * @return
     */
    @Override
    public int read(byte[] d) {
        return in.backRead(d);
    }
    
    @Override
    public int read(byte[] d, int offset, int length) {
        return in.backRead(d, offset, length);
    }

    @Override
    public RevInputStream getBackInputStream() {
        return this;
    }

    @Override
    public int backRead() {
        return in.backRead();
    }

    @Override
    public int backRead(byte[] d, int offset, int length) {
        return in.backRead(d, offset, length);
    }

    @Override
    public int backRead(byte[] d) {
        return in.backRead(d, 0, d.length);
    }
    
    @Override
    public int backSize() {
        return in.backSize();
    }

    @Override
    public Packet backSplit(long length) {
        return in.backSplit(length);
    }

    @Override
    public long back(long length) {
        return in.back(length);
    }

    @Override
    public byte revGet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
