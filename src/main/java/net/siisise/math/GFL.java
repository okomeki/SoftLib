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
package net.siisise.math;

import net.siisise.lang.Bin;

/**
 * 128bit用 ガロア拡大体らしきもの.
 * GF(2^128)
 * 
 */
public class GFL {
    
    static final long FF128 = 0x87; 
    
    long constRb = FF128;
    long[] constRbl;
    
    private final long[][] shL;

    /**
     * 
     * @param rb 
     * @param a 
     */
    public GFL(long rb, long[] a) {
        constRb = rb;
        
        shL = new long[a.length * 64][];
        shL[0] = a.clone();
        for ( int i = 1; i< shL.length; i++) {
            shL[i] = shl(shL[i-1]);
        }
    }
    
    public GFL(long[] a) {
        this(FF128, a);
    }
    
    public GFL(long[] rb, long[] a) {
        constRbl = rb.clone();
        shL = new long[a.length * 64][];
        shL[0] = a.clone();
        for ( int i = 1; i < shL.length; i++) {
            shL[i] = shll(shL[i-1]);
        }
    }
    
    /**
     * 左シフト演算っぽい動作 1つ.
     * s・2.
     * 名前は未定.
     * @param s
     * @return s・2
     */
    public final long[] shl(long[] s) {
        long[] v = Bin.shl(s);
        v[v.length - 1] ^= (constRb * (s[0] >>> 63));
        return v;
    }

    
    public final long[] shll(long[] s) {
        long[] v = Bin.shl(s);
        if (s[0] < 0) {
            Bin.xorl(v, constRbl);
        }
        return v;
    }

    /**
     * 右シフト演算っぽい動作.
     * s/2.
     * 名前は未定.
     * @param s
     * @return 
     */
    public long[] shr(long[] s) {
        long[] v = Bin.ror(s); // constRbの1bit が消えるのでrorで付ける
        if ( v[0] < 0) {
            v[v.length - 1] ^= constRb >>> 1;
        }
        return v;
    }
    
    public long[] mul(long[] b) {
        long[] v = new long[b.length];
        int k = 128;
        for (int i = 0; i < b.length; i++) {
            long c = b[i];
            for ( int j = 0; j < 64; j++ ) {
                k--;
                if ( c << j < 0) {
                    Bin.xorl(v, shL[k]);
                }
            }
        }
        return v;
    }

    /**
     * 
     * @param s
     * @return 
     */
    public long[] lfsrLeft(long[] s) {
        throw new UnsupportedOperationException();
    }
    
    public long[] lfsrRight(long[] s) {
        throw new UnsupportedOperationException();
    }

    
}
