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

/**
 *
 */
public interface ReadableBlock extends Block {
    
    /**
     * データなし -1
     * @return 
     */
    int read();
    int read(byte[] data);
    int read(byte[] data, int offset, int length);
    
    int backRead();
    int backRead(byte[] data);
    int backRead(byte[] data, int offset, int length);
    
    // Packet互換のあれこれ
    long length();
    int size();
    byte[] toByteArray();
    InputStream getInputStream();
    InputStream getBackInputStream();
    
}
