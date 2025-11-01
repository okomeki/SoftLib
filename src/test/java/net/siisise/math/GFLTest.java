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

import java.math.BigInteger;
import net.siisise.lang.Bin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GFの可変サイズ Test.
 * CMAC ぐらいを参考にするとよい?
 */
public class GFLTest {
    
    public GFLTest() {
    }

    /**
     * Test of shl method, of class GFL.
     * https://www.nuee.nagoya-u.ac.jp/labs/tiwata/omac/tv/omac1-tv.txt
     */
    @Test
    public void testX() {
        System.out.println("gfl.x");
        long[] s = {0xe568f68194cf76d6l,0x174d4cc04310a854l};
        
        GFL instance = new GFL(GFL.GF128);
        long[] expResult = {0xcad1ed03299eedacl, 0x2e9a99808621502fl};
        long[] result = instance.x(s);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of shr method, of class GFL.
     * https://www.nuee.nagoya-u.ac.jp/labs/tiwata/omac/tv/omac1-tv.txt
     */
    @Test
    public void testR() {
        System.out.println("gfl.r");
        long[] s = {0xcad1ed03299eedacl, 0x2e9a99808621502fl};
        GFL instance = new GFL(GFL.GF128);
        long[] expResult = {0xe568f68194cf76d6l,0x174d4cc04310a854l};
        long[] result = instance.r(s);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of mul method, of class GFL.
     */
    @Test
    public void testMul() {
        System.out.println("mul");
        long[] b = {0,1,1};
        long[] a = {0,0,3};
        GFL instance = new GFL(a, GFL.GF128);
        long[] expResult = new long[] {0,3,3};
        long[] result = instance.mul(b);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of pow method, of class GFL.
     */
    @Test
    public void testPow() {
        System.out.println("pow");
        long[] a = {0,0,3};
        long[] b = {0,0,2};
        GFL instance = new GFL(a, GFL.GF128);
        long[] expResult = new long[] {0,0,5};
        long[] result = instance.pow(a,b);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of lfsrLeft method, of class GFL.
     */
/*
    @Test
    public void testLfsrLeft() {
        System.out.println("lfsrLeft");
        long[] s = null;
        GFL instance = null;
        long[] expResult = null;
        long[] result = instance.lfsrLeft(s);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of lfsrRight method, of class GFL.
     */
/*
    @Test
    public void testLfsrRight() {
        System.out.println("lfsrRight");
        long[] s = null;
        GFL instance = null;
        long[] expResult = null;
        long[] result = instance.lfsrRight(s);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    @Test
    public void testGF() {
        System.out.println("GFFFF");
        byte[] a = Bin.toByteArray("0388dace60b6a392f328c2b971b2fe78");
        byte[] b = Bin.toByteArray("66E94BD4EF8A2C3B884CFA59CA342B2E");
        byte[] ex = Bin.toByteArray("519FA38AC731568E9C1EB21731167F1C");
        byte[] ONE = Bin.toByteArray("00000000000000000000000000000001");
        GFL gf = new GFL(GFL.GF128);
        long[] la = Bin.btol(a);
        long[] LONG_ONE = Bin.btol(ONE);
        long[] c = gf.mul(la, Bin.btol(b));
        byte[] d = gf.mul(a, b);
        long[] ia = gf.inv(la);
        System.out.println(" a      " + Bin.toUpperHex(a));
        System.out.println(" a x b  " + Bin.toUpperHex(Bin.ltob(c)));
        System.out.println(" a x bl " + Bin.toUpperHex(d));
        System.out.println(" a inv  " + Bin.toUpperHex(Bin.ltob(ia)));
        System.out.println(" a mul  " + Bin.toUpperHex(Bin.ltob( gf.mul(la, ia))));
        assertArrayEquals(Bin.btol(ex),c);
        assertArrayEquals(ex,d);
        assertArrayEquals(LONG_ONE,gf.mul(la, ia));
        
    }
    
    @Test
    public void testInv() {
        System.out.println("GFL.inv");
        BigInteger p = BigInteger.ONE.shiftLeft(409).add(BigInteger.ONE.shiftLeft(87)).add(BigInteger.ONE);
        byte[] order = Bin.toByteArray("010000000000000000000000000000000000000000000000000001e2aad6a612f33307be5fa47c3c9e052f838164cd37d9a21173");
        byte[] K    = Bin.toByteArray("6A0B81D9320B5C305D730B1C1E74B03FAFB88A7EC355990B75F9B70E8532433296A32492CBA06F8583D5B19C5B8C5D6D07EC");
        byte[] Kinv = Bin.toByteArray("A202EA455D0E1A5EF09054B39259C768DB76FFD1A77B6281FC7056A4A23A1012CDD604E4D7993E0D9EDD422DEFD782C1225A1A");
        GFL gf = new GFL(p);
        GFL gfo = new GFL(order);
        long[] Ol = GFL.toLong(order);
        long[] Kl = GFL.toLong(K);
        long[] Kinvl = GFL.toLong(Kinv);
        long[] in = gfo.inv(Kl);
        System.out.println(" order    " + Bin.toUpperHex(order));
        System.out.println(" order    " + Bin.toUpperHex(Bin.ltob(Ol)));
//        System.out.println(" K ex     " + Bin.toUpperHex(K));
        System.out.println(" K ex     " + Bin.toUpperHex(Bin.ltob(Kl)));
        System.out.println(" inv      " + Bin.toUpperHex(Bin.ltob(in)));
//        System.out.println(" Kinv ex  " + Bin.toUpperHex(Kinv));
        System.out.println(" Kinv ex  " + Bin.toUpperHex(Bin.ltob(Kinvl)));
        byte[] r1 = gfo.mul(K,Kinv);
        byte[] r2 = Bin.ltob(gfo.mul(Kl,in));
        System.out.println(" r1     " + Bin.toUpperHex(r1));
        System.out.println(" r2     " + Bin.toUpperHex(r2));
        
    }
}
