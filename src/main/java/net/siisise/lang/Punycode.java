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

import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.math.Matics;

/**
 * RFC 3492 Punycode
 * 参考 https://qiita.com/msmania/items/dc0e2b8c2c5de0707435
 */
public class Punycode {
    private static final int BASE = 36;
    private static final int TMIN = 1;
    private static final int TMAX = 26;
    private static final int SKEW = 38;
    private static final int DAMP = 700;
    private static final int INITIAL_BIAS = 72;
    private static final int INITIAL_N = 128;
    private static final char DELIMIT = '-';

    /**
     * 国際化ドメイン名をACE xn-- ASCII列に変換する
     * @param u
     * @return Punycode
     */
    public static java.lang.String toASCII(java.lang.String u) {
        CodePoint cp = new CodePoint(u);
        if ( cp.length() >= 64 ) {
            throw new IllegalStateException();
        }
        int[] cpch = cp.codePoints().toArray();
        
        Packet st = new PacketA();
        
        // 分離とソート unicodeの大きい方から code と位置に変換
        do {
            int index = -1;
            int co = 0; // (n, i)
            for (int i = cpch.length - 1; i >= 0; i--) {
                if ( cpch[i] > co ) {
                    index = i;
                    co = cpch[i];
                }
            }
            if ( co < 128 ) {
                break;
            }
            co = co * cpch.length + index;
            st.backWrite(Bin.toByte(co));
            
            int[] tmpch = new int[cpch.length - 1];
            System.arraycopy(cpch, 0, tmpch, 0, index);
            System.arraycopy(cpch, index+1, tmpch, index, tmpch.length - index);
            cpch = tmpch;
        } while ( true );
        // 残ったのがASCII
        StringBuilder sb = new StringBuilder();
        for (int c : cpch) {
            sb.append((char)c); // ASCIIのみ
        }
        if (sb.length() > 0) {
            // ASCII あり
            // xn-- を付ける場合 ASCII + 国際化両方あり
            sb.append('-');
        }
        if (st.size() == 0) { // 国際化なし ASCIIのみ
            return sb.toString();
        }

        // delta変換
        int n = INITIAL_N;
        int bias = INITIAL_BIAS;
        
        byte[] dc = new byte[4];
        int tn = cpch.length;
        int c = n * tn - 1;
        tn++;
        int d = DAMP;
        while (st.length() > 0) {
            int ostat = c + n + 1;
            st.read(dc);
            c = Bin.btoi(dc)[0];
            n = c / tn;
            int delta = c - ostat;
            sb.append(toCh(delta, bias)); // delta からコード
            bias = adapt(delta, d, tn);
            d = 2;
            tn++;
        }
        
        return sb.toString();
    }

    /**
     * bias の重み.
     * @param delta 前の差分
     * @param div
     * @param tn n番目の文字 (1開始)
     * @return 
     */
    private static int adapt(int delta, int div, int tn) {
        // 1.
        delta /= div;
        // 2.
        delta += delta / tn;
        int n = 0;
        while (delta > ((BASE - TMIN) * TMAX) / 2) {
            delta /= BASE - TMIN;
            n++;
        }
        return (BASE * n) + (((BASE - TMIN + 1) * delta) / (delta + SKEW));
        
    }
    
    /**
     * 下からj桁目ぐらいの閾値 thresholds
     * BASE(36) * (j+1) - bias
     * 最小 TMIN 最大 TMAX に制限
     * @param j
     * @param bias 可変値
     * @return t_j
     */
    private static int t(int j, int bias) {
        return Matics.range(BASE * (j+1) - bias, TMIN, TMAX);
    }

    static final char[] CODE = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'};

    /**
     * 1文字デルタからコード
     * BASE = 36
     * @param n delta
     * @return 
     */
    private static java.lang.String toCh(int n, int bias) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int k = t(i, bias);
        while (n >= k) {
            n -= k;
            sb.append(CODE[k + (n % (BASE - k))]);
            n = n / (BASE - k);
            i++;
            k = t(i, bias);
        }
        sb.append(CODE[n]);
        return sb.toString();
    }

    public static java.lang.String toUnicode(java.lang.String a) {
        int delimit_index = a.lastIndexOf(DELIMIT);
        StringBuilder sb = new StringBuilder();
        if ( delimit_index >= 0) {
            java.lang.String ascii = a.substring(0,delimit_index);
            if (delimit_index == a.length() - 1) {
                return ascii;
            }
            sb.append(ascii);
        }
        
        char[] ex = a.substring(delimit_index + 1).toCharArray();
        int of = 0;
        int w = 1;
        int bias = INITIAL_BIAS;
        int d = DAMP;
        int n = 0;
        int tn = sb.codePointCount(0, sb.length()) + 1;
        int c = INITIAL_N * tn;
        for (int i = 0; i < ex.length; i++) {
            int k = t(i - of, bias);
            int m = num(ex[i]);
            n += m * w;
            if (m >= k) {
                w *= BASE - k;
            } else {
                c += n;
                int idx = c % tn;
                c /= tn;
                char[] cp = Character.toChars(c);
                sb.insert(sb.offsetByCodePoints(0, idx), cp);

                // 次の文字
                of = i+1;
                bias = adapt(n, d, tn);
                tn++;
                c = c*tn + idx + 1;
                
                d = 2;
                w = 1;
                n = 0;
            }
        }
        
        return sb.toString();
    }
    
    static int num(char ch) {
        if ( ch >= 'a' && ch <= 'z') {
            return ch - 'a';
        } else if ( ch >= 'A' && ch <= 'Z') {
            return ch - 'A';
        } else if ( ch >= '0' && ch <= '9') {
            return ch - '0' + 26;
        }
        throw new IllegalStateException();
    }

}
