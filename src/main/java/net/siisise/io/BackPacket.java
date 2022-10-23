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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * flushとcloseは持っていない、Exceptionも発生しない。
 * InputStream, OutputStream と少し仕様/動作が異なる。
 */
public interface BackPacket {

    OutputStream getOutputStream();
    InputStream getBackInputStream();

    void write(int data);
    void write(byte[] data, int offset, int length);
    void write(byte[] data);
    void write(FrontPacket pac);
    void dwrite(byte[] data);

    int backRead();
    int backRead(byte[] data, int offset, int length);
    int backRead(byte[] data);
 
}
