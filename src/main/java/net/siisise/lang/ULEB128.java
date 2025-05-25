/*
 * Copyright 2025 okome.
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
package net.siisise.lang;

import java.math.BigInteger;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;

/**
 * 符号なし可変長整数符号化.
 * Little Endian
 * Wasm用仮
 */
public class ULEB128 {
    
    static final BigInteger MINUS1 = BigInteger.valueOf(-1);
    
    /**
     * 正の整数限定
     * @param val
     * @return 
     */
    public static byte[] toULEB128(BigInteger val) {
        Packet pac = new PacketA();
        
        do {
            byte v = (byte)(val.intValue() & 0x7f);
            val = val.shiftRight(7);
            if (!(val.equals(BigInteger.ZERO) || val.equals(MINUS1))) {
                v |= 0x80;
            }
            pac.write(v);
        } while (val.compareTo(MINUS1) != 0);
        return pac.toByteArray();
    }
}
