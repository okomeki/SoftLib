/*
 * Copyright 2023 okome.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Stream っぽいものをまとめ Arraysっぽいもの
 */
public class Blocks {
    
    /**
     * ReadableBlock, OverBlock をStream用に分割する風味.
     * 暗号用などにコピーを防ぎながらどうにかする
     * @param <B> ブロック型
     * @param src 
     * @param blockSize ブロックサイズ
     * @return 分割されたブロック列
     */
    public static <B extends ReadableBlock> List<B> subBlocks(B src, int blockSize) {
        List<B> blocks = new ArrayList<>();
        long count = src.length() / blockSize;
        for ( int i = 0; i < count; i++ ) {
            blocks.add((B)src.sub(i * blockSize, blockSize));
        }
        return blocks;
    }
    
    public static List<OverBlock> subBlocks(byte[] src, int blockSize) {
        return subBlocks(OverBlock.wrap(src), blockSize);
    }

}
