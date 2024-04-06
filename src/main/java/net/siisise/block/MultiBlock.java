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
import net.siisise.math.Matics;

/**
 * 複数Blockをまとめて1ブロックっぽくする.
 * 中のblock のseek 位置は初期化されるので利用できない。
 * 別々のポインタを扱いたいときはsubBlockにしてから格納する.
 * EditBlockにするかどうか未定.
 */
public class MultiBlock extends OverBlock.AbstractSubOverBlock {

    List<OverBlock> blocks;
    int number;
//    long subOffset;

    /**
     * 複数ブロックをまとめて1つのBlockにする.
     * @param blocks 
     */
    public MultiBlock(List<OverBlock> blocks) {
        super(blocks);
        this.blocks = new ArrayList<>(blocks);
        number = 0;
    }

    /**
     * 前にBlockを追加する.
     * 中のBlockの個別利用はできない.
     * @param block 追加するブロック
     */
    
    public void addDirectPrev(OverBlock block) {
        block.seek(block.length());
        blocks.add(0, block);
        number++;
    } 
    
    /**
     * 後にBlockを追加する.
     * 中のBlockの個別利用はできない.
     * @param block 追加するブロック
     */
    public void addDirectNext(OverBlock block) {
        block.seek(-block.backLength());
        blocks.add(block);
    }

    /**
     * 前にBlockを追加する.
     * ポインタを別々で管理する.
     * @param block 追加するブロック
     */
    public void addSubPrev(OverBlock block) {
        addDirectPrev(new SubOverBlock(block));
    } 

    /**
     * 後にBlockを追加する.
     * ポインタを別々で管理する.
     * @param block 追加するブロック
     */
    public void addSubNext(OverBlock block) {
        addDirectNext(new SubOverBlock(block));
    }

    /**
     * 読み.
     *
     * @param buf
     * @param offset
     * @param length
     * @return
     */
    @Override
    public int read(byte[] buf, int offset, int length) {
        int st = offset;
        int last = offset + length;
        int size = blocks.size();

        do {
            OverBlock sub = blocks.get(number);
//            long p = sub.backLength();
            int ss = sub.read(buf, offset, last - offset);
            offset += ss;
            number++;
        } while (number < size && offset < last);
        number--;
        return offset - st;
    }

    /**
     * 逆読み.
     *
     * @param buf 容器
     * @param offset 位置
     * @param length 長さ
     * @return 読んだ長さ
     */
    @Override
    public int backRead(byte[] buf, int offset, int length) {
        int st = offset + length;
        int en = st;

        do {
            OverBlock sub = blocks.get(number);

            int ss = Math.min(sub.backSize(), length);
            st -= ss;
            int s = sub.backRead(buf, st, ss);
            length -= s;
            number--;
        } while (number >= 0 && length > 0);
        number++;
        return en - st;
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        if (!Matics.sorted(0, offset, offset + length, data.length)) {
            throw new java.nio.BufferOverflowException();
        }
        length = Math.min(length, size());

        int last = offset + length;
        int size = blocks.size();

        do {
            OverBlock sub = blocks.get(number);
//            long p = sub.backLength();
            int ss = Math.min(last - offset, sub.size());
            sub.write(data, offset, ss);
            offset += ss;
            number++;
        } while (number < size && offset < last);
        number--;
    }

    /**
     * 戻り書く.
     * @param data データ
     * @param offset data内のデータの開始位置
     * @param length データサイズ
     */
    @Override
    public void backWrite(byte[] data, int offset, int length) {
        if ( !Matics.sorted(0,length, backSize())) {
            throw new java.nio.BufferOverflowException();
        }
//        put(backSize() - length, data, offset, length);
//        back(length);

        int st = offset + length;

        do {
            OverBlock sub = blocks.get(number);

            int ss = Math.min(sub.backSize(), length);
            st -= ss;
            int s = sub.backRead(data, st, ss);
            length -= s;
            number--;
        } while (number >= 0 && length > 0);
        number++;
    }
}
