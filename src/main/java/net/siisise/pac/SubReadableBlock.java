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
 *
 */
public abstract class SubReadableBlock extends AbstractReadableBlock {
    /**
     * 最小位置.
     * java.nio.Buffer の arrayOffset()
     */
    protected final int min;
    /**
     * 最大位置.
     * block.length の代わり
     * java.nio.Buffer の limit
     */
    protected final int max;
    protected int pos = 0;
    
    SubReadableBlock(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * 範囲内で移動する.
     * @param position
     * @return 位置.
     */
    @Override
    public int seek(int position) {
        if (position + min >= max) {
            pos = max;
        } else if ( position > 0 ) {
            pos = min + position;
        } else {
            pos = min;
        }
        
        return pos - min;
    }

    /**
     * 
     * @param length マイナスも使えるといい
     * @return 
     */
    @Override
    public int skip(int length) {
        if ( length < 0) {
            return back(-length);
        }
        int size = Integer.min(max - pos , length);
        pos += size;
        return size;
    }

    @Override
    public int back(int length) {
        if ( length <= pos - min ) {
            pos -= length;
        } else {
            length = pos - min;
            pos = min;
        }
        return length;
    }

    @Override
    public long length() {
        return max - pos;
    }

    @Override
    public int size() {
        return max - pos;
    }
    
    /**
     * position だったもの.
     * @return 読み込み済みのサイズ position
     */
    @Override
    public int backSize() {
        return pos - min;
    }
}
