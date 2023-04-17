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

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 仮組かもしれない.
 */
public class BASE58 implements TextEncode {
    
    static final char[] BITCOIN = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    static byte[] RBIT = new byte[128];
    static final BigInteger FE = BigInteger.valueOf(58);

    static {
        Arrays.fill(RBIT, (byte)0xff);
        for ( byte i = 0; i < BITCOIN.length; i++ ) {
            RBIT[BITCOIN[i]] = (byte)i;
        }
    }
    
    @Override
    public String encode(byte[] bytes, int offset, int length) {
        int zpad = 0;
        byte[] tmp = Arrays.copyOfRange(bytes, offset, offset + length);
        for ( int i = 0; i < tmp.length; i++ ) {
            if ( tmp[i] != 0) {
                zpad = i;
                break;
            }
        }
        BigInteger bi = new BigInteger(1, tmp);
        StringBuilder sb = new StringBuilder();
        
        while ( !bi.equals( BigInteger.ZERO) ) {
            BigInteger[] da = bi.divideAndRemainder(FE);
            sb.insert(0,BITCOIN[da[1].intValue()]);
            bi = da[0];
        }
        if ( zpad > 0 ) {
            char[] t = new char[zpad];
            Arrays.fill(t, BITCOIN[0]);
            sb.insert(0,t);
        }
        return sb.toString();
    }

    @Override
    public byte[] decode(String encoded) {
        BigInteger v = BigInteger.ZERO;
        char[] chs = encoded.toCharArray();
        int zpad = chs.length;
        for (int i = 0; i < chs.length; i++ ) {
            if (chs[i] != BITCOIN[0]) {
                zpad = i;
                break;
            }
        }
        
        for ( char ch : chs ) {
            byte r = RBIT[ch];
            v = v.multiply(FE).add(BigInteger.valueOf(r));
        }
        int len = (v.bitLength() + 7) / 8;
        byte[] tmp = v.toByteArray();
        if ( tmp.length < len + zpad ) {
            byte[] n = new byte[len + zpad];
            System.arraycopy(tmp, 0, n, len - tmp.length, tmp.length);
            tmp = n;
        }
        if ( tmp.length > len + zpad ) {  // 桁数は減ることもありそう
            byte[] n = new byte[len + zpad];
            System.arraycopy(tmp, tmp.length - n.length, n, 0, n.length);
        }
        return tmp;
    }
    
}
