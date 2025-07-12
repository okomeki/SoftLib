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

import java.text.Normalizer;

/**
 * IDNAの簡易版.
 * IDNA 2003 か 2008 準拠予定。(まだ)
 * 区切り文字は IDNA2003 U+ も可能とした。 とりあえず static
 * で作ってあとで分ける
 *
 */
@Deprecated
public class IDNA {

    // IDNA2003 の区切りも可能としている
    private static final java.lang.String SEPARATER = "[\\u002E\\u3002\\uFF0E\\uFF61]";

    static enum LDH_TYPE {
        NON_LDH, // bin, bit
          UNDER_LABEL,
          U_LABEL,
    //  LDH,
          NR_LDH,
          R_LDH, // ??--
    //      BQ_LABEL, // bq-- RACE 廃止
    //      XN_LABEL, // xn--
              FAKE_A_LABEL,
              A_LABEL, // valid xn label
    }

    /**
     * ラベルの判定.
     * U_LABEL,A_LABEL,NR_LDHとUNDER_LABELぐらいが正常
     * NON_LDH, は正規化が必要かも
     * R_LDH 未対応または不正
     * FAKE_A_LABEL は不正
     *
     * @param a ラベル
     * @return 判定
     */
    static LDH_TYPE type(java.lang.String a) {
        if (!isLDH(a)) {
            if (isASCII(a)) {
                if (a.charAt(0) == '_' && isLDH(a.substring(1))) { // 仮
                    return LDH_TYPE.UNDER_LABEL;
                }
                return LDH_TYPE.NON_LDH;
            } else {
                // U-label かもしれない
                return isUlabel2008(a) && Punycode.toASCII(a).length() < 60 ? LDH_TYPE.U_LABEL : LDH_TYPE.NON_LDH;
            }
        } else { // LDH
            if (a.length() > 4 && a.toLowerCase().startsWith("xn--")) {
                // Aラベル または 疑似Aラベル
                return isAlabel2008(a) ? LDH_TYPE.A_LABEL : LDH_TYPE.FAKE_A_LABEL;
            } else if (isR_LDH(a)) { // ??--
                return LDH_TYPE.R_LDH; // 不明 旧RACEなど
            } else {
                return LDH_TYPE.NR_LDH; // 正規
            }
        }
    }

    /**
     * A-label 判定.
     *
     * @param a LDH判定済み
     * @return まだてきとう
     */
    private static boolean isAlabel2008(java.lang.String a) {
        // isLDHで最後に-が来ることはない
        try {
            java.lang.String u = Punycode.toUnicode(a.substring(4));
            return isUlabel2008(u);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * U-label 判定.
     *
     * @param u 候補
     * @return 判定
     */
    private static boolean isUlabel2008(java.lang.String u) {
        java.lang.String c = Normalizer.normalize(u, Normalizer.Form.NFC);
        if (!c.equals(u) || u.toLowerCase().startsWith("xn--")) {
            return false;
        }
        // ToDo: その他判定

//        throw new UnsupportedOperationException();
        return true; // 仮
    }

    /**
     * Letter Digit Hyphn
     *
     * @param a
     * @return
     */
    private static boolean isLDH(java.lang.String a) {
        int[] chs = a.chars().toArray();
        for (int ch : chs) {
            if (ch < 0x2d || (ch > 0x2d && ch < 0x30) || (ch > 0x39 && ch < 0x41) || (ch > 0x5a && ch < 0x61) || (ch > 0x7a)) {
                return false;
            }
        }
        if (chs[0] == '-' || chs[chs.length - 1] == '-') {
            return false;
        }
        return true;
    }

    /**
     * U-ラベル? ACE可能か判定.
     *
     * @param a
     * @return
     */
    private static boolean isASCII(java.lang.String a) {
        int[] chs = a.chars().toArray();
        for (int ch : chs) {
            if (ch > 0x7f) {
                return false;
            }
        }
        return true;
    }

    /**
     * R_LDHの判定.
     *
     * @param a
     * @return
     */
    private static boolean isR_LDH(java.lang.String a) {
        return a.length() > 4 && a.substring(2, 4).equals("--");
    }

    /**
     * ドメインの変換.
     * 検索用途では先に正規化しておくといい
     * @param u 一般ドメイン
     * @return A-label側に揃えたドメイン
     */
    public static java.lang.String toASCII(java.lang.String u) {
        // U+002E U+3002 U+FF0E U+FF61
        java.lang.String[] sp = u.split(SEPARATER); // IDNA 2008 ではピリオドのみ
        java.lang.String[] t = new java.lang.String[sp.length];
        for (int i = 0; i < t.length; i++) {
            t[i] = toASCIILabel(NFKC(sp[i]));
        }
        return java.lang.String.join(".", t);
    }

    /**
     * 仮 NFKCかけるだけ.
     * @param u 国際化文字列
     * @return NFKCかけてみた
     */
    static final java.lang.String NFKC(java.lang.String u) {
        return Normalizer.normalize(u, Normalizer.Form.NFKC);
    }

    /**
     * U-ラベルを正規化、A-ラベルに変換する。
     * その他は変換しない
     *
     * @param u U-ラベル
     * @return A-ラベル
     */
    public static java.lang.String toASCIILabel(java.lang.String u) {
        u = NFKC(u).toLowerCase();
        LDH_TYPE t = type(u);
        if (t != LDH_TYPE.U_LABEL) {
            return u;
        }
        return "xn--" + Punycode.toASCII(u);
    }

    /**
     * ドメインをUnicode側へ変換.
     * フィルタしていないので注意.
     *
     * @param a 一般ドメイン
     * @return U-label側にそろえた
     *
     */
    public static java.lang.String toUnicode(java.lang.String a) {
        java.lang.String[] sp = a.split(SEPARATER);
        java.lang.String[] t = new java.lang.String[sp.length];
        for (int i = 0; i < t.length; i++) {
            t[i] = toUnicodeLabel(sp[i]);
        }
        return java.lang.String.join(".", t);
    }

    /**
     * A-ラベルを正規化されたU-ラベルに変換する。
     * フィルタしていないので注意.
     * 逆変換の保証はしない
     * その他は変換しない
     *
     * @param a A-ラベル
     * @return U-ラベル
     */
    public static java.lang.String toUnicodeLabel(java.lang.String a) {
        LDH_TYPE t = type(a);
        if (t != LDH_TYPE.A_LABEL) {
            return a;
        }
        java.lang.String u = Punycode.toUnicode(a.substring(4).toLowerCase());
        return NFKC(u);
    }
}
