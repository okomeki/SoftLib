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

/**
 * Packet のデータ加工点を中間にしたもの.
 * Stream は便利だがseekとか柔軟にしたいこともあるので戻れるStreamの定番に.
 */
public interface Block {

    /**
     * 先頭から編集位置までのoffset
     * @return offset
     */
    int getOffset();
    /**
     * offset まで移動する.
     * 足りない場合は最後尾に移動する. 余白は追加しない
     * @param offset 位置 相対
     * @return 移動した位置
     */
    int seek(int offset);

    /**
     * 相対移動.
     * @param offset
     * @return 
     */
    int skip(int offset);

    /**
     * 相対的に戻る.
     * 長い場合は先頭くらいまで.
     * @param length 戻るサイズ
     * @return 戻ったサイズ
     */
    int back(int length);
}
