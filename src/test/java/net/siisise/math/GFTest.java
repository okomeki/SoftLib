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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import net.siisise.lang.Bin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author okome
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
}
