/*
 * Copyright 2019-2022 Siisise Net.
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
 * 文字列・改
 * UNICODE内文字列操作のstaticメソッドは、基本的にここに作成する。
 * 同一名称で違う機能に関わるメソッドのみ別クラスに実装する。
 * 商用ライセンスは、個別相談
 * コンパイル環境によって動作が異なってしまう。
 */
public class String {

    /** 全角を使うべき半角文字 */
    static final java.lang.String 半角カタカナ = "ｱｲｳｴｵｧｨｩｪｫｶｷｸｹｺｻｼｽｾｿﾀﾁﾂｯﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖｬｭｮﾗﾘﾙﾚﾛﾜｦﾝ｡､･｢｣ｰﾞﾟ";
    static final java.lang.String カタカナ = "アイウエオァィゥェォカキクケコサシスセソタチツッテトナニヌネノハヒフヘホマミムメモヤユヨャュョラリルレロワヲン。、・「」ー゛゜";
    //    static final java.lang.String ひらがな = "あいうえおぁぃぅぇぉかきくけこさしすせそたちつってとなにぬねのはひふへほまみむめもやゆよゃゅょらりるれろわをんー゛゜、。がぎぐげござじずぜぞだぢづでどヴばびぶべぼぱぴぷぺぽ";
/*
    private static final java.lang.String toFull =
        "　！”＃＄％＆’（）＊＋、－．／０１２３４５６７８９：；＜＝＞？"+
        "＠ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ［￥］＾＿"+
        "｀ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ｛｜｝～"+
          "。「」、・ヲァィゥェォャュョッーアイウエオカキクケコサシスセソ"+
        "タチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワン゛゜";

    private static final java.lang.String fromFull =
            " !\"#$%&'()*+,-./0123456789:;<=>?"+
            "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"+
            "`abcdefghijklmnopqrstuvwxyz{|}~"+
             "｡｢｣､･ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿ"+
             "ﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝﾞﾟ";
*/
    /**
     * 半角カタカナを全角カタカナに変換する日本語正規化
     * 1文字単位。
     * @param kana
     * @return 
     */
    public static char toKanaFull(char kana) {
        int index;

        if ((index = 半角カタカナ.indexOf(kana)) >= 0) {
            kana = カタカナ.charAt(index);
        }

        return kana;
    }

    /**
     * 半角カタカナを全角カタカナにし、濁点も統合する。
     * 日本語正規化
     * 使用例 郵便番号辞書
     * JPRS RACEドメイン
     * @param str
     * @return 
     */
    public static java.lang.String toKanaFull(java.lang.String str) {
        StringBuffer str2;
        char kkv;
        str2 = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            kkv = toKanaFull(str.charAt(i));
            if (i > 0) {
                if (kkv == '゛' || kkv == '\u3099') {
                    kkv = str2.charAt(str2.length() - 1);
                    if ("カキクケコサシスセソタチツテトハヒフヘホ".indexOf(kkv) >= 0) {
                        kkv++;
                        str2.deleteCharAt(str2.length() - 1);
                    } else if (kkv == 'ウ') {
                        kkv = 'ヴ';
                        str2.deleteCharAt(str2.length() - 1);
                    } else {
                        kkv = '゛';
                    }
                } else if (kkv == '゜' || kkv == '\u309a') {
                    kkv = str2.charAt(str2.length() - 1);
                    if ("ハヒフヘホ".indexOf(kkv) >= 0) {
                        kkv += 2;
                        str2.deleteCharAt(str2.length() - 1);
                    } else {
                        kkv = '゜';
                    }
                }
            }
            str2.append(kkv);

        }

        return str2.toString();
    }

    /**
     * 日本語正規化
     * 全角/半角カタカナをひらがなにする
     * @param str
     * @return 
     */
    public static java.lang.String kanaUpper(java.lang.String str) {
        StringBuffer str2;
        str2 = new StringBuffer();

        char ch;
        str = toKanaFull(str);
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            //idx = kana2.indexOf(ch);
            if (ch >= 0x30A0 && ch <= 0x30FA) { // 3093 までがよい? ヴ3094までがよい?

                ch -= 0x60;
            }
            str2.append(ch);
        }
        return str2.toString();
    }

    /**
     * ひらがなを全角カタカナにする
     * @param str
     * @return 
     */
    public static java.lang.String kanaLower(java.lang.String str) {
        StringBuffer str2;
        str2 = new StringBuffer(str.length());
        str.chars().forEach(ch -> {
            if ( ch >= 0x3040 && ch <= 0x309A) {
                ch+= 0x60;
            }
            str2.append((char)ch);
        });
        return str2.toString();
    }
    // Windows で記述する場合
    /** 半角にすべき文字 */
    static final java.lang.String MS全角記号 = "＋＊／＝｜！？”＃＠＄％＆’｀（）［］｛｝，．；：＿＜＞＾－";
    static java.lang.String 全角記号;
    /**
     * Shift_JIS、ISO-2022-JPとWIndows_31Jで配置が異なる文字
     */
    static final java.lang.String MS932非互換文字 = "～－―∥￠￡￢";
    static java.lang.String SJIS非互換文字;
    static java.lang.String CP943C非互換文字;
    static final java.lang.String 半角記号 = "+*/=|!?\"#@$%&'`()[]{},.;:_<>^-";
    

    static {
        try {
            全角記号 = new java.lang.String(MS全角記号.getBytes("Windows-31J"), "Shift_JIS");
            SJIS非互換文字 = new java.lang.String(MS932非互換文字.getBytes("Windows-31J"), "Shift_JIS");
            CP943C非互換文字 = new java.lang.String(MS932非互換文字.getBytes("Windows-31J"), "Cp943C");
        } catch (java.io.UnsupportedEncodingException e) {
        }
    }

    /**
     * 英数字列を半角文字に正規化する。
     * 未完全版。
     * 郵便番号、電話番号、日本語ドメイン等
     * @param str
     * @return 
     * @since JDK1.4.1
     */
    public static java.lang.String toHalf(java.lang.String str) {
        StringBuffer str2;
        if ( str == null ) return null;
        str2 = new StringBuffer();
        char ch;
        int idx;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ch >= 'ａ' && ch <= 'ｚ') {
                ch += 'a' - 'ａ';
            } else if (ch >= 'Ａ' && ch <= 'Ｚ') {
                ch += 'A' - 'Ａ';
            } else if (ch >= '０' && ch <= '９') {
                ch += '0' - '０';
            } else if (ch == '　') {
                ch = ' ';
            } else if ((idx = MS全角記号.indexOf(ch)) >= 0) {
                ch = 半角記号.charAt(idx);
            } else if ((idx = 全角記号.indexOf(ch)) >= 0) {
                ch = 半角記号.charAt(idx);
            // } else if (ch == '￥') { // 判断は微妙
            //     ch = '\\';
            }
            str2.append(ch);
        }
        return str2.toString();
    }

    public java.lang.String toFull( java.lang.String str ) {
        StringBuffer str2;
        if ( str == null ) return null;
        str2 = new StringBuffer();
        char ch;
        int idx;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ch >= 'a' && ch <= 'z') {
                ch += 'ａ' - 'a';
            } else if (ch >= 'A' && ch <= 'Z') {
                ch += 'Ａ' - 'A';
            } else if (ch >= '0' && ch <= '9') {
                ch += '０' - '0';
            } else if (ch == ' ') {
                ch = '　';
            } else if ((idx = 半角記号.indexOf(ch)) >= 0) {
                ch = MS全角記号.charAt(idx);
            } else if ((idx = 全角記号.indexOf(ch)) >= 0) {
                ch = MS全角記号.charAt(idx);
            } else if (ch == '\\') { // 判断は微妙
                ch = '￥';
            }
            str2.append(ch);
        }
        return toKanaFull( str2.toString() );
    }

    /**
     * SJIS、EUCの全角判定
     * 制御コード、半角文字は false
     * @param ch
     * @return 
     */
    public static boolean isFull(char ch) {
        if (ch >= 0x100) {
            return true;
        } else if (半角カタカナ.indexOf(ch) >= 0) {
            return false;
        } else {
            return false;
        }
    }

    /**
     * 文字コード互換変換
     * MS932固有の文字は置き換えない
     * @param c
     * @return 
     */
    public static char toMS932Map(char c) {
        int index;
        index = SJIS非互換文字.indexOf(c);
        if (index >= 0) {
            c = MS932非互換文字.charAt(index);
        }
        return c;
    }

    /**
     * UTF-16対応済?
     * @param c
     * @return 
     */
    public static int toMS932Map(int c) {
        int index;
        index = SJIS非互換文字.indexOf(c);
        if (index >= 0) {
            c = MS932非互換文字.charAt(index);
        // 非互換文字指定にサロゲートペアが含まれる場合、次の処理に変える
        //    index = SJIS非互換文字.codePointCount(0,index);
        //    c = MS932非互換文字.codePointAt(index);
        }
        return c;
    }

    /**
     * 文字列のMS932化
     * MS932固有の文字は置き換えない
     * @param str
     * @return 
     */
    public static java.lang.String toMS932Map(java.lang.String str) {
        char[] cbuf2 = new char[str.length()];
        for (int i = 0; i < cbuf2.length; i++) {
            cbuf2[i] = toMS932Map(str.charAt(i));
        }
        return java.lang.String.copyValueOf(cbuf2);
    }

    /**
     * 文字列のMS932化
     * MS932固有の文字は置き換えない
     * @param cbuf
     * @return 
     */
    public static char[] toMS932Map(char[] cbuf) {
        char[] cbuf2 = new char[cbuf.length];
        for (int i = 0; i < cbuf.length; i++) {
            cbuf2[i] = toMS932Map(cbuf[i]);
        }
        return cbuf2;
    }

    /**
     * EUC-JP、ISO-2022-JP、Shift_JISに対応するコード位置に文字を変換する
     * MS932固有の文字は置き換えない
     * @param c MS932系のコードマップ文字
     * @return ShiftJIS系のコードマップ文字
     */
    public static char toSJISMap(char c) {
        int index;
        index = MS932非互換文字.indexOf(c);
        if (index >= 0) {
            c = SJIS非互換文字.charAt(index);
        }
        return c;
    }

    /**
     * EUC-JP、ISO-2022-JP、Shift_JISに対応するコード位置に文字を変換する
     * MS932固有の文字は置き換えない
     * @param cbuf
     * @return 
     */
    public static char[] toSJISMap(char[] cbuf) {
        char[] cbuf2 = new char[cbuf.length];
        for (int i = 0; i < cbuf.length; i++) {
            cbuf2[i] = toSJISMap(cbuf[i]);
        }
        return cbuf2;
    }

    /**
     * EUC-JP、ISO-2022-JP、Shift_JISに対応するコード位置に文字を変換する
     * MS932固有の文字は置き換えない
     * @param str MS932系のコードマップ文字
     * @return 
     */
    public static java.lang.String toSJISMap(java.lang.String str) {
        char[] cbuf2 = new char[str.length()];
        //StringBuffer bf;
        //bf = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) {
            cbuf2[i] = toSJISMap(str.charAt(i));
        }
        return java.lang.String.copyValueOf(cbuf2);
    }

    /**
     * 文字コード互換変換。
     * MS932固有の文字は置き換えない
     * @param c
     */
    public static char toCP943CMap(char c) {
        int index;
        index = SJIS非互換文字.indexOf(c);
        if (index >= 0) {
            c = CP943C非互換文字.charAt(index);
        } else {
            index = MS932非互換文字.indexOf(c);
            if (index >= 0) {
                c = CP943C非互換文字.charAt(index);
            }
        }
        return c;
    }

    /**
     * 文字列のMS932化
     * MS932固有の文字は置き換えない
     * @param str
     * @return 
     */
    public static java.lang.String toCP943CMap(java.lang.String str) {
        char[] cbuf2 = new char[str.length()];
        for (int i = 0; i < cbuf2.length; i++) {
            cbuf2[i] = toCP943CMap(str.charAt(i));
        }
        return java.lang.String.copyValueOf(cbuf2);
    }

    /**
     * 文字列のMS932化
     * MS932固有の文字は置き換えない
     * @param cbuf
     * @return 
     */
    public static char[] toCP943CMap(char[] cbuf) {
        char[] cbuf2 = new char[cbuf.length];
        for (int i = 0; i < cbuf.length; i++) {
            cbuf2[i] = toCP943CMap(cbuf[i]);
        }
        return cbuf2;
    }
}
