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

import java.util.Arrays;

/**
 * RFC 4648 The Base16, Base32, and Base64 Data Encodings.
 * Bech32はBASE32互換部分のみ。デコードのみ可。エンコードは別で作った。
 * 
 * FIXはいくつかの誤字をそれっぽく解釈する.
 */
public class BASE32 {

    public static enum Type {
        BASE32("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"),
        BASE32FIX("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"),
        BASE32HEX("0123456789ABCDEFGHIJKLMNOPQRSTUV"),
        Bech32("qpzry9x8gf2tvdw0s3jn54khce6mua7l"),
        Bech32FIX("qpzry9x8gf2tvdw0s3jn54khce6mua7l");
        final char[] ENC = new char[32];
        final byte[] DEC = new byte[128];

        Type(String code) {
            Arrays.fill(DEC, (byte) -1);
            for (byte i = 0; i < ENC.length; i++) {
                char ch = code.charAt(i);
                ENC[i] = ch;
                DEC[ch] = i;
                if (ch >= 'A' && ch <= 'Z') {
                    DEC[ch + 32] = i;
                } else if (ch >= 'a' && ch <= 'z') {
                    DEC[ch - 32] = i;
                }
            }
        }
    }

    static {
        // 勝手に補正
        Type.BASE32FIX.DEC['0'] = 14;  // O
        Type.BASE32FIX.DEC['1'] = 8;  // I
        Type.BASE32FIX.DEC['8'] = 1;  // B
        Type.Bech32FIX.DEC['1'] = 31; // l
        Type.Bech32FIX.DEC['b'] = 26; // 6
        Type.Bech32FIX.DEC['B'] = 7; // 8
        Type.Bech32FIX.DEC['i'] = 18; // j (または l)
        Type.Bech32FIX.DEC['I'] = 31; // l
        Type.Bech32FIX.DEC['o'] = 15; // 0
        Type.Bech32FIX.DEC['O'] = 15; // 0
    }

    private char[] enc;
    private byte[] dec;

    public static final Type BASE32 = Type.BASE32;
    public static final Type BASE32HEX = Type.BASE32HEX;
    public static final Type Bech32 = Type.Bech32;

    public BASE32() {
        this(Type.BASE32);
    }

    public BASE32(Type type) {
        if (type != null) {
            enc = type.ENC;
            dec = type.DEC;
        }
    }

    /**
     * いろいろありそうなので特殊な配置用.
     * 大文字小文字は区別しないこと.
     *
     * @param code 並びパターン ASCII32文字
     */
    public BASE32(String code) {
        enc = new char[32];
        dec = new byte[128];
        Arrays.fill(dec, (byte) -1);
        for (byte i = 0; i < 32; i++) {
            char ch = code.charAt(i);
            enc[i] = ch;
            dec[ch] = i;
            if (ch >= 'A' && ch <= 'Z') {
                dec[ch + 32] = i;
            } else if (ch >= 'a' && ch <= 'z') {
                dec[ch - 32] = i;
            }
        }
    }

    public String encode(byte[] src) {
        return encode(src, 0, src.length);
    }

    /**
     * BASE32エンコード. ビット処理はBitPacketに任せた.
     *
     * @param src バイト列
     * @param offset 位置
     * @param length 長さ
     * @return BASE32
     */
    public String encode(byte[] src, int offset, int length) {
        BigBitPacket bp = new BigBitPacket();
        bp.write(src, offset, length);
        return encode(bp);
    }

    public String encode(BigBitPacket bp) {
        int r = (int) bp.bitLength() % 5;
        if (r > 0) { // 端数
            bp.writeBit(0, 5 - r);
        }
        int len = (int) (bp.bitLength() / 5);
        char[] encd = new char[len];
        for (int i = 0; i < len; i++) {
            encd[i] = enc[bp.readInt(5)];
        }
        return new String(encd);
    }

    /**
     * BESE32デコード. データ用。
     * チェックサムは削られるのでBech32などでは不向き. 対象外の文字は無視する.
     *
     * @param src BASE32
     * @return バイト列 余りは捨てる.
     */
    public byte[] decode(String src) {
        BitPacket bp = decodePacket(src);
        int r = (int) (bp.bitLength() % 8);
        bp.backReadInt(r);
        return bp.toByteArray();
    }

    /**
     * チェックサム付きのBash32デコード用.
     *
     * @param src
     * @return ビット単位のパケット
     */
    public BigBitPacket decodePacket(String src) {
        char[] chs = src.toCharArray();
        BigBitPacket bp = new BigBitPacket();
        for (char ch : chs) {
            if (ch < 128) {
                int d = dec[ch];
                if (d >= 0) { // 知らない文字は無視する
                    bp.writeBit(d, 5);
                }
            }
        }
        return bp;
    }
}
