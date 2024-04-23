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
package net.siisise.lang;

/**
 * IEEE 754 の 16bit 形式.
 * 中はshortをつかう.
 * CBORの符号化用に用意しただけ.
 */
public class Binary16 extends Number {

    public static final int BYTES = 2;
    public static final int MAX_EXPONENT = 15;
    public static final int MIN_EXPONENT = -14;
//    static final short qNaN = 0x7e00;
//    static final short sNaN = (short)0xff00;
    public static final short NaN = 0x7e00;
    public static final short NEGATIVE_INFINITY = (short) 0xFC00;
    public static final short POSITIVE_INFINITY = (short) 0x7C00;
    public static final int SIZE = 16;

    private static final short EXPONENT_MASK = 0x7c00;
    private static final short FRACTION_MASK = 0x03ff;

    final short value;

    /**
     * 使わない方がいいのかも valueOf が標準
     * @param v
     * @deprecated
     */
    @Deprecated
    public Binary16(short v) {
        value = v;
    }
    
    /**
     * float に変換してからintにする
     * @return intっぽい値
     */
    @Override
    public int intValue() {
        return (int) floatValue();
    }

    /**
     * floatに変換してからlongにする
     * @return longっぽい値
     */
    @Override
    public long longValue() {
        return (long) floatValue();
    }

    /**
     * てきとーに変換.
     * @return floatっぽい値
     */
    @Override
    public float floatValue() {
        return binary16BitsToFloat(value);
    }

    /**
     * floatにしてからdoubleに変換.
     * @return doubleっぽい値
     */
    @Override
    public double doubleValue() {
        return (double) floatValue();
    }

    /**
     * floatにしてからshortに変換した値.
     * @return shortっぽい値
     */
    @Override
    public short shortValue() {
        return (short) floatValue();
    }
    
    /**
     * short で中身.
     * @return binary16な中身
     */
    public short binary16Value() {
        return value;
    }

    /**
     * short 形式をBinary16形式にするだけ.
     * 型自動変換に注意.
     * @param value Binary16っぽい値
     * @return
     */
    public static Binary16 valueOf(short value) {
        return new Binary16(value);
    }
    
    public static Binary16 valueOf(java.lang.String s) throws NumberFormatException {
        return new Binary16(parseBinary16(s));
    }
    
    /**
     * 文字列をIEEE形式でどうにかする.
     * 文字列をfloat に変えてからBinary16にしているので誤差などあるかもしれない.
     * @param s
     * @return
     * @throws NumberFormatException 
     */
    public static short parseBinary16(java.lang.String s) throws NumberFormatException {
        float f = Float.parseFloat(s);
        return Binary16.FloatToBinary16bits(f);
    }

    /**
     * Binary16 を float に拡張するよ 1bit flag 5bit exponent 10bit fraction 非正規化数は
     * てきとーに正規化内に変換した
     *
     * @param b16 binary16 format IEEE 754
     * @return
     */
    public static float binary16BitsToFloat(short b16) {
        int sign = ((b16 & 0x8000) << 16); // 符号部 1 bit
        int exponent = (b16 & EXPONENT_MASK) >> 10; // 指数部 5 bit
        int fraction = b16 & FRACTION_MASK; // 仮数部 10 bit

        switch (exponent) {
            case 0: // 非正規化数? または 0
                if (fraction != 0) { // 正規化可能な気がする
                    // 正規化するとき
                    while (((fraction & 0x200) == 0)) {
                        exponent--;
                        fraction <<= 1;
                    }
                    exponent--;
                    fraction <<= 1;
                    fraction &= FRACTION_MASK;
                    exponent += 0x70;
                }
                break;
            case 31: // 非数 NaN, 無限大 固定値にしておく?
                if (fraction != 0) {
                    return Float.NaN;
                } else {
                    return sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                }
            default:
                exponent += 0x70; //  0x7f - 0x0f; // 112
//                fraction <<= 13;
        }

        return Float.intBitsToFloat(sign | (exponent << 23) | (fraction << 13));
    }

    /**
     * NaN をまとめる
     *
     * @param v
     * @return
     */
    public static short binary16ToShortBits(short v) {
        return isNaN(v) ? NaN : v;
    }

    public static short binary16ToRawShortBits(short v) {
        return v;
    }

    /**
     * 1 to 1 bit 符号部
     * sign 5 to 8 bit 指数部
     * 10 to 23 bit 仮数部
     *
     * @param val
     * @return
     */
    public static short FloatToBinary16bits(float val) {
        int f = Float.floatToRawIntBits(val);
        // 全桁とっておく
        int sign = (f >> 16) & 0x8000;
        int exponent = (f >> 13) & 0x3fc00; // 全桁とっておく
        int fraction = f & 0x7fffff;

        switch (exponent) {
            case 0: // 非正規化数? または 0
                if (fraction != 0) { // 表現不能
                    fraction = 0;
                }
            case 0x3fc00:
                if (fraction != 0) { // NaN の保存
                    // 上位ビットを保存する
                    if ((fraction & 0x7fe000) == 0) {
                        fraction = 0x2000; // 1ビットは残す
                    }
                } else { // INFINITY
                    return (sign == 0) ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
                }
            default:
                exponent -= 0x70; //  0x7f - 0x0f; // 112
                if ((exponent & (-1 - 0x1f)) != 0) {
                    // ToDo: 非正規化数に変換できればする
                    throw new java.lang.UnsupportedOperationException("まだない");
                }
        }
        exponent &= EXPONENT_MASK;
        fraction &= FRACTION_MASK;

        return (short) (sign | exponent | fraction);

    }

    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * NaN 判定
     * @param value 16bit列
     * @return true NaN false その他
     */
    public static boolean isNaN(short value) {
        return ((value & 0x7c00) == 0x7c00) && ((value & 0x3ff) != 0);
    }

    /**
     * てきとー NaNなどの扱い注意
     *
     * @param obj
     * @return 浮動小数点的な比較
     */
    @Override
    public boolean equals(Object obj) {
        if (!isNaN() && obj instanceof Binary16) {
            Binary16 o16 = (Binary16) obj;
            // NaN 以外は比較可能
            return !o16.isNaN() && o16.value == value; // 非NaN同士
        }
        return false; // NaN, 他の型
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.value;
        return hash;
    }

    /**
     * 
     * Float経由で文字列化.
     * @param b binary16 ビット列
     * @return 浮動小数点表現だといいな
     */
    public static java.lang.String toString(short b) {
        Float f = Binary16.binary16BitsToFloat(b);
        return f.toString();
    }
}
