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
 * java.nio Buffer っぽいかな
 * 違い mark は offset を取得して外部で管理する.
 * Read Only.
 * 切り取りが可能. min と max が固定できるため.
 * 
 * 0スタートではない。min 以下は読み書き禁止.
 * 
 * Buffer 0 開始位置
 * Block 0 配列の頭
 * Buffer mark 位置のキープ (0または1つ)
 * Block offset 複数位置のキープ (外部)
 * Buffer position 現在位置
 * Block offset 現在位置
 * Buffer limit データ上限 以降空白
 * Block limit相当なし
 * Buffer capacity 最大容量
 * Block max 最大読み込み可能範囲 参照clip範囲の最大
 * Block length 実体最大
 * 
 * Buffer   0    mark position  limit capacity
 * Block  0 min  x    offset          max   length
 * 
 * BNFで配列っぽい振る舞いの構造がほしかった.
 * Stream は便利だがseekとか柔軟にしたいこともあるので戻れるStreamの定番に.
 */
public interface Block {

    /**
     * offset まで移動する.
     * 足りない場合は最後尾に移動する. 余白は追加しない
     * @param offset 位置 相対
     * @return 移動した位置
     */
    int seek(int offset);

    /**
     * 相対移動.
     * @param length
     * @return 
     */
    int skip(int length);

    /**
     * 相対的に戻る.
     * skip の逆
     * 長い場合は先頭くらいまで.
     * @param length 戻るサイズ
     * @return 戻ったサイズ
     */
    int back(int length);
}
