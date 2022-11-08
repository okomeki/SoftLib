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
 * サイズ変更が可能な編集.
 */
public interface IndexEdit extends IndexInput,IndexOutput {
    
    byte del(long index);
    void del(long index, long size);
    IndexEdit del(long index, byte[] d);
    IndexEdit del(long index, byte[] d, int offset, int length);

    /**
     * 追加.
     * @param index
     * @param d 
     */
    void add(long index, byte d);
    void add(long index, byte[] d);
    void add(long index, byte[] d, int srcOffset, int length);
}