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

import net.siisise.io.BackPacket;
import net.siisise.io.FrontPacket;

/**
 * 編集点が中央になったPacket.
 * 先頭、終端が編集点とは別に存在する。
 * 上書き、切り取りの概念を追加する。
 */
public interface EditBlock extends ReadableBlock, FrontPacket, BackPacket {

    /**
     * 上書き
     * @param data
     * @return 
     */
    int overWrite(int data);
    int overWrite(byte[] data);
    int overWrite(byte[] data, int offset, int length);
    
    /**
     * 切り取る.
     * 編集可能な場合のみ、不要なサイズを切り取る.
     * @param length 長さ
     * @return 切り取ったデータ.
     */
    byte[] drop(int length);
    byte[] backDrop(int length);
    
}
