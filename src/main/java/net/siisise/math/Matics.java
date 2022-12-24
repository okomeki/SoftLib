/*
 * Copyright 2022 okome.
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
package net.siisise.math;

/**
 * 仮 名前は変わるかも.
 */
public class Matics {

    /**
     * 複数の値からの最小.
     * @param v 候補
     * @return 最小値
     */
    public static int min(int... v) {
        int r = Integer.MAX_VALUE;
        for (int i = 0; i < v.length; i++) {
            if ( r > v[i] ) {
                r = v[i];
            }
        }
        return r;
    }

    /**
     * 複数の値からの最小.
     * @param v 候補
     * @return 最小値
     */
    public static long min(long... v) {
        long r = Long.MAX_VALUE;
        for (int i = 0; i < v.length; i++) {
            if ( r > v[i] ) {
                r = v[i];
            }
        }
        return r;
    }

    /**
     * 複数の値からの最大.
     * @param v 候補
     * @return 最大値
     */
    public static int max(int... v) {
        int r = Integer.MIN_VALUE;
        for (int i = 0; i < v.length; i++) {
            if ( r < v[i] ) {
                r = v[i];
            }
        }
        return r;
    }

    /**
     * 複数の値からの最大.
     * @param v 候補
     * @return 最大値
     */
    public static long max(long... v) {
        long r = Long.MIN_VALUE;
        for (int i = 0; i < v.length; i++) {
            if ( r < v[i] ) {
                r = v[i];
            }
        }
        return r;
    }

    /**
     * 数値を範囲内に丸める.
     * @param v 元
     * @param min 最小値
     * @param max 最大値
     * @return vを最小値と最大値の間に丸めた値
     * @see #sorted(int...) 判定のみ 
     */
    public static int range(int v, int min, int max) {
        return ( v < min ) ? min : ( v > max ) ? max : v; 
    }

    /**
     * 数値を範囲内に丸める.
     * @param v 元
     * @param min 最小値
     * @param max 最大値
     * @return vを最小値と最大値の間に丸めた値
     */
    public static long range(long v, long min, long max) {
        return ( v < min ) ? min : ( v > max ) ? max : v; 
    }

    /**
     * 複数の大小を比較したいこともある.
     * java.math.Math#min(int,int)
     * @param v 要素
     * @return 昇順
     */
    public static boolean sorted(int... v) {
        for ( int i = 0; i < v.length -1; i++ ) {
            if ( v[i] > v[i+1] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * 複数の大小を比較したいこともある.
     * java.math.Math#min(int,int)
     * @param v 要素
     * @return 昇順
     */
    public static boolean sorted(long... v) {
        for ( int i = 0; i < v.length -1; i++ ) {
            if ( v[i] > v[i+1] ) {
                return false;
            }
        }
        return true;
    }
}
