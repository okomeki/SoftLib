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
package net.siisise.io;

import java.util.HashMap;
import java.util.Map;

/**
 * 1行、複数行もち.
 * 名前は仮
 */
public interface TextEncode {

    /**
     * バイト列をテキストに変換する.
     * @param bytes 元データ
     * @return テキスト符号化文字列
     */
    default String encode(byte[] bytes) {
        return encode(bytes, 0, bytes.length);
    }

    String encode(byte[] bytes, int offset, int length);

    /**
     * テキストからバイト列を復号する.
     * パスワード等は未対応.
     * @param encoded テキスト
     * @return バイト列
     */
    byte[] decode(String encoded);
    
    /**
     * ヘッダなど付加データを別で持つものをデコードする場合
     * key null がデータのよてい
     * @param base64
     * @return 
     */
    default Map<String,Object> decodeMap(String base64) {
        Map<String, Object> map = new HashMap<>();
        map.put(null, decode(base64));
        return map;
    }
}
