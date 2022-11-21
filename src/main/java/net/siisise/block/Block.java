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
 * Block 複数位置のキープ (外部のみ)
 * Buffer position 現在位置
 * Block backSize 現在位置
 * Buffer limit データ上限 以降空白
 * Block 可変limitなし backSize + size のみ
 * Buffer capacity 最大容量
 * Block backSize + size 最大読み込み可能範囲 参照clip範囲の最大
 * Buufer remaining
 * Block size
 * 
 * Buffer   0    mark position  limit capacity
 * Block  0 min  x    offset          max backSize +size
 * 
 * BNFで配列っぽい振る舞いの構造がほしかった.
 * Stream は便利だがseekとか柔軟にしたいこともあるので戻れるStreamの定番に.
 */
public interface Block {

    /**
     * offset まで範囲内で移動する.
     * Block内の指定の位置に移動する.
     * 足りない場合は最後尾に移動する. 余白は追加しない
     * Buffer の position と同じ.
     * 参照するのは backLength()
     * 切り取った場合もSubBlockの先頭が0でReadableBlock,OverBlockで範囲を超えることはない。
     * 
     * @param offset 位置 相対
     * @return 移動した位置
     */
    long seek(long offset);

    /**
     * 部分集合をつくる.
     * 範囲を超えて読み書きはできない。
     * メモリ空間は共有される.
     * flip などで使う.
     * SubBlock の interface型は親と同じ範囲を想定しているが書けないこともない。権威移譲はてきとう。
     * 
     * @param index 位置
     * @param length サイズ ToDo:長すぎたときどうするかは未定
     * @return 分割されたブロック
     */
    Block sub(long index, long length);

    /**
     * 分ける.
     * OverBlock 以下は親と同じ型.EditBlockは実装によってOverBlockやReadableBlockかも。
     * split の前版
     * メモリ空間は可能な場合共有.
     * @see net.siisise.io.Packet#split(long)
     * @return 前 
     */
    Block flip();
}
