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
 * Output を OutputとOutputStream に変える
 */
public class FilterOutput extends AbstractOutput {

    private final Output out;
    
    public FilterOutput(Output out) {
        this.out = out;
    }

    @Override
    public void write(int b) {
        out.write(b);
    }
    
    @Override
    public void write(byte[] b, int offset, int length) {
        out.write(b, offset, length);
    }
    
    @Override
    public void dwrite(byte[] b) {
        out.dwrite(b);
    }
    
    @Override
    public void write(Input pac) {
        out.write(pac);
    }

    @Override
    public Output put(byte[] data, int offset, int length) {
        return out.put(data, offset, length);
    }

}
