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
package net.siisise.math;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import net.siisise.lang.Bin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class GFTest {
    
    public GFTest() {
    }

    /**
     * Test of r method, of class GF.
     * @throws java.security.NoSuchAlgorithmException
     */
    @Test
    public void testR() throws NoSuchAlgorithmException {
        System.out.println("r");
        byte[] s = new byte[] {-60,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        GF gf = new GF(128,GF.FF128);
        
        byte[] expResult = s;
        byte[] m = gf.x(s);
        System.out.println(Bin.toHex(s));
        System.out.println(Bin.toHex(m));
        byte[] result = gf.r(m);
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);
        m = gf.r(s);
        result = gf.x(m);
        System.out.println(Bin.toHex(s));
        System.out.println(Bin.toHex(m));
        System.out.println("res:" + Bin.toHex(result));
        assertArrayEquals(s, result);
        SecureRandom rnd = SecureRandom.getInstanceStrong();
        rnd.nextBytes(s);
        result = gf.r(gf.x(s));
        assertArrayEquals(s, result);
        result = gf.x(gf.r(s));
        assertArrayEquals(s, result);
    }

    /**
     * Test of x method, of class GF.
     */
    @Test
    public void testX_byteArr() {
        System.out.println("x");
//        byte[] s = {-28,4,2,1,6,9,3,0,80,40,20,10,60,90,30,5};
        byte[] s = {(byte)0x80,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
        GF gf = new GF(128,GF.FF128);
        byte[] expResult = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,(byte)0x85};
        byte[] result = gf.x(s);
        assertArrayEquals(expResult, result);
        s = new byte[] {(byte)0x80};
        gf = new GF();
        expResult = new byte[] {0x1b};
        result = gf.x(s);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of x method, of class GF.
     */
    @Test
    public void testX_longArr() {
        System.out.println("x");
        long[] s = {0x8000000000000000l,1};
        GF gf = new GF(128,GF.FF128);
        long[] expResult = {0,0x85};
        long[] result = gf.x(s);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of x method, of class GF.
     */
    @Test
    public void testX_int() {
        System.out.println("x");
        int a = 0x80;
        GF instance = new GF();
        int expResult = 0x1b;
        int result = instance.x(a);
        assertEquals(expResult, result);
    }

    /**
     *  3  0b00000011
     *  5  0b00000101
     * 17  0b00010001
     * 0x11b 0b100011011 283 256 + 16 + 8 + 2 + 1
     */
    @Test
    public void testXXX() {
        System.out.println();
        BigInteger ff = BigInteger.valueOf(0x11b);
        GF gf = new GF(8, 0x11b);
        // 100011011
        // 279
        int x = 3;
        int x5 = 5;
        for ( int i = 1; i < 0x100; i++ ) {
            System.out.print("x = 0x" + Integer.toHexString(i));
            System.out.print(" = " + i);
            System.out.print(" 2x = " + Integer.toHexString(gf.x(i)));
            System.out.print(" x^2 = " + Integer.toHexString(gf.mul(i,i)));
            System.out.print(" x^2 + x = 0x" + Integer.toHexString(gf.mul(i,i)));
            System.out.print(" = " + gf.mul(i,i));
            byte[] n = new byte[1];
            n[0] = (byte)i;
            n = gf.inv(n);
            System.out.print(" inv = 0x" + Integer.toHexString(n[0]));
            System.out.print(" = " + Integer.toHexString(gf.inv(i)));
            System.out.print(" = " + gf.inv(i));
            System.out.print(" 3^"+i+"= " + Integer.toHexString(x));
            System.out.print(" inv = 0x" + Integer.toHexString(gf.inv(x)));
            x = gf.mul(x,3);
            System.out.print(" 5^"+i+"= " + Integer.toHexString(x5));
            System.out.print(" inv = 0x" + Integer.toHexString(gf.inv(x5)));
            x5 = gf.mul(x5,5);
            System.out.print(" mod3 " + i % 3);
            System.out.print(" mod5 " + i % 5);
            System.out.print(" mod17 " + i % 17);
            System.out.print(" mod3 " + gf.inv(i) % 3);
            System.out.print(" mod5 " + gf.inv(i) % 5);
            System.out.println(" mod17 " + gf.inv(i) % 17);
        }
        
        
        System.out.println("e 6 inv " + gf.mul(0x8d, 0xf6));
        System.out.println("e 6 inv " + gf.inv(gf.mul(0x8d, 0xf6)));
    }
    
    public void testLongGF() {
        System.out.println("longGF");
        GF gf = new GF(8,GF.FF8);
        byte[] a = new byte[1];
        byte[] b = new byte[1];
        byte[] c;
        c = gf.mul(a, b);
        System.out.println(Bin.toHex(c));
    }
}
