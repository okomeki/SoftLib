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
import net.siisise.block.ReadableBlock;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;

/**
 * 符号なしとして符号化.
 * Wasm用仮
 */
public class LEB128 {

    /**
     * 正の整数限定
     * @param val
     * @return 
     */
    public static byte[] toLEB128(BigInteger val) {
        Packet pac = new PacketA();
        
        do {
            byte v = (byte)(val.intValue() & 0x7f);
            val = val.shiftRight(7);
            if (!val.equals(BigInteger.ZERO)) {
                v |= 0x80;
            }
            pac.write(v);
        } while (val.compareTo(BigInteger.ZERO) > 0);
        return pac.toByteArray();
    }

    public static BigInteger toBigInteger(byte[] val) {
        return toBigInteger(ReadableBlock.wrap(val));
/*
        int shift = 0;
        BigInteger r = BigInteger.ZERO;
        for (byte v : val) {
            BigInteger b = BigInteger.valueOf(v & 0x7f);
            
            r = r.or(b.shiftLeft(shift));
            if ( (v & 0x80) == 0) {
                return r;
            }
            shift += 7;
        }
        throw new IllegalStateException();
*/
    }

    public static BigInteger toBigInteger(Input in) {
        int shift = 0;
        BigInteger r = BigInteger.ZERO;
        int v = in.read();
        while (v >= 0) {
            BigInteger b = BigInteger.valueOf(v & 0x7f);
            
            r = r.or(b.shiftLeft(shift));
            if ( (v & 0x80) == 0) {
                return r;
            }
            shift += 7;
            v = in.read();
        }
        throw new IllegalStateException();
    }

    public static long toLong(Input in) {
        int shift = 0;
        long r = 0;
        int v = in.read();
        while (v >= 0) {
            long b = v & 0x7f;
            
            r |= b << shift;
            if ( (v & 0x80) == 0) {
                return r;
            }
            shift += 7;
            v = in.read();
        }
        throw new IllegalStateException();
    }
    
    /**
     * 符号なし
     * @param sign 符号なしとみなす
     * @return 
     */
    public static byte[] toLEB128(long sign) {
        Packet pac = new PacketA();
        do {
            byte v = (byte)(sign & 0x7f);
            sign >>>= 7;
            if ( sign != 0) {
                v |= 0x80;
            }
            pac.write(v);
        } while (sign > 0);
        return pac.toByteArray();
    }
}
