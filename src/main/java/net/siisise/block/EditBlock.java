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
package net.siisise.block;

import net.siisise.io.FrontPacket;
import net.siisise.io.IndexEdit;
import net.siisise.io.PacketA;

/**
 * 編集点が中央になったPacket.
 * 先頭、終端が編集点とは別に存在する。
 * 上書き、切り取りの概念を追加する。
 */
public interface EditBlock extends OverBlock, IndexEdit {

    /**
     * 切り取る.
     * 不要なサイズを切り取る.
     * del と類似.
     * 
     * @param length 長さ
     * @return 切り取ったデータ.
     */
    byte[] drop(int length);
    byte[] backDrop(int length);

    /**
     * dを元にして編集可能.
     * 複製あり.
     * 
     * @param d 元データ
     * @return 編集可能ブロック
     */
    public static EditBlock wrap(byte[] d) {
        return new SinglePacketBlock(d);
    }

    /**
     * dを元にして編集可能.
     * 
     * @param d 元データ
     * @param offset 位置
     * @param length サイズ
     * @return 編集可能ブロック
     */
    public static EditBlock wrap(byte[] d, int offset, int length) {
        SinglePacketBlock b = new SinglePacketBlock();
        b.put(d,offset,length);
        b.seek(0);
        return b;
    }

    public static EditBlock wrap(FrontPacket p) {
        return new PacketBlock(p);
    }

    public static EditBlock wrap(PacketA p) {
        return new SinglePacketBlock(p);
    }
}
