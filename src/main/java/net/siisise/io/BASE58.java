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
 * Base58を可変長にしたもの.
 * その他サイズでもよい。
 */
public class BASE58 implements TextEncode {

    static final String BITCOIN = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    static final String RIPPLE  = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz";

    private char[] enc;
    private short[] rbit;
    private BigInteger FE;
    
    public static final BASE58 BTC = new BASE58(BITCOIN);
    public static final BASE58 XRP = new BASE58(RIPPLE);

    public BASE58() {
        this(BITCOIN);
    }

    /**
     * 
     * @param code 符号に使用するASCII文字列 code 127まで サロゲートペア未
     */
    public BASE58(String code) {
        enc = code.toCharArray();
        FE = BigInteger.valueOf(enc.length);
        int max = 0;
        for (int i = 0; i < enc.length; i++) {
            if ( enc[i] > max) {
                max = enc[i];
            }
        }
        max++;
        rbit = new short[max];
        Arrays.fill(rbit, (short)max);
        for (int i = 0; i < enc.length; i++) {
            rbit[enc[i]] = (byte) i;
        }
    }

    /**
     * BASE58 符号化
     *
     * @param bytes 元データ
     * @param offset 符号化開始位置
     * @param length サイズ
     * @return 符号化文字列
     */
    @Override
    public String encode(byte[] bytes, int offset, int length) {
        int zpad = 0;
        byte[] tmp = Arrays.copyOfRange(bytes, offset, offset + length);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != 0) {
                zpad = i;
                break;
            }
        }
        BigInteger bi = new BigInteger(1, tmp);
        StringBuilder sb = new StringBuilder();

        while (!bi.equals(BigInteger.ZERO)) {
            BigInteger[] dar = bi.divideAndRemainder(FE);
            sb.insert(0, enc[dar[1].intValue()]);
            bi = dar[0];
        }
        if (zpad > 0) {
            char[] t = new char[zpad];
            Arrays.fill(t, enc[0]);
            sb.insert(0, t);
        }
        return sb.toString();
    }

    @Override
    public byte[] decode(String encoded) {
        BigInteger v = BigInteger.ZERO;
        char[] chs = encoded.toCharArray();
        int zpad = chs.length;
        for (int i = 0; i < chs.length; i++) {
            if (chs[i] != enc[0]) {
                zpad = i;
                break;
            }
        }

        for (char ch : chs) {
            if ( ch >= rbit.length) {
                throw new java.lang.IllegalStateException();
            }
            int r = rbit[ch];
            if ( r >= rbit.length ) {
                throw new java.lang.IllegalStateException();
            }
            v = v.multiply(FE).add(BigInteger.valueOf(r));
        }
        int len = (v.bitLength() + 7) / 8;
        byte[] tmp = v.toByteArray();
        if (tmp.length < len + zpad) {
            byte[] n = new byte[len + zpad];
            System.arraycopy(tmp, 0, n, len - tmp.length, tmp.length);
            tmp = n;
        }
        if (tmp.length > len + zpad) {  // 桁数は減ることもありそう
            byte[] n = new byte[len + zpad];
            System.arraycopy(tmp, tmp.length - n.length, n, 0, n.length);
        }
        return tmp;
    }

}
