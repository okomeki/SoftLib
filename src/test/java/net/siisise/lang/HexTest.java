/*
 * Copyright 2022 Siisise Net.
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

import java.math.BigInteger;
import net.siisise.io.BASE64;
import net.siisise.io.FileIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class HexTest {
    
    public HexTest() {
    }

    /**
     * Test of toHex method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToHex() {
        System.out.println("toHex");
        byte[] src = new byte[] {0x01,0x2f,(byte)0xe3,(byte)0x80};
        java.lang.String expResult = "012fe380";
        java.lang.String result = Hex.toHex(src);
        assertEquals(expResult, result);
    }

    /**
     * Test of toUpperHex method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToUpperHex() {
        System.out.println("toUpperHex");
        byte[] src = new byte[] {0x01,0x2f,(byte)0xe3,(byte)0x80};
        java.lang.String expResult = "012FE380";
        java.lang.String result = Hex.toUpperHex(src);
        assertEquals(expResult, result);
    }

    /**
     * Test of toByteArray method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToByteArray_String() {
        System.out.println("toByteArray");
        java.lang.String src = "012fE567";
        byte[] expResult = new byte[] {0x01,0x2f,(byte)0xe5,0x67};
        byte[] result = Hex.toByteArray(src);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByteArray method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToByteArray_CharSequence() {
        System.out.println("toByteArray");
        CharSequence src = "01234567";
        byte[] expResult = new byte[] {0x01,0x23,0x45,0x67};
        byte[] result = Hex.toByteArray(src);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByteArray method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToByteArray_charArr() {
        System.out.println("toByteArray");
        char[] txt = "01234567".toCharArray();
        byte[] expResult = new byte[] {0x01,0x23,0x45,0x67};
        byte[] result = Hex.toByteArray(txt);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByteArray method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToByteArray_3args() {
        System.out.println("toByteArray");
        char[] txt = "01234567".toCharArray();
        int offset = 1;
        int length = 4;
        byte[] expResult = new byte[] {0x12,0x34};
        byte[] result = Hex.toByteArray(txt, offset, length);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByteArray method, of class Hex.
     */
    @Test
    public void testToByteArray_BigInteger_int() {
        System.out.println("toByteArray");
        byte[] exp = new byte[] {0x00, 0, 0x01,0x2f,(byte)0xe5,0x67};
        BigInteger num = new BigInteger(exp);
        System.out.println(num.toString());
        System.out.println(num.toString(16));
        System.out.println(Hex.toHex(exp));
        int length = exp.length;
        byte[] expResult = exp;
        byte[] result = Hex.toByteArray(num, length);
        assertArrayEquals(expResult, result);

        exp = new byte[] {(byte)0xff, (byte)0xff, 0x01,0x2f,(byte)0xe5,0x67};
        num = new BigInteger(exp);
        System.out.println(num.toString());
        System.out.println(num.toString(16));
        System.out.println(Hex.toHex(exp));
        length = exp.length;
        expResult = exp;
        result = Hex.toByteArray(num, length);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByteArray method, of class Hex.
     */
    @Test
    public void testToByteArray_charArr_int() {
        System.out.println("toByteArray(char[],radix)");
        byte[] exp = new byte[] {0x01,0x2f,(byte)0xe5,0x67};
        BigInteger num = new BigInteger(exp);
        BASE64 x64 = new BASE64(BASE64.HEX64,0);
        java.lang.String txt = x64.encode(exp);
        int radix = 6;
//        txt = num.toString(radix);
//        java.lang.String txt = "0000012fe567";
        System.out.println("hex64: " + txt);
        byte[] expResult = exp;
        byte[] result = Hex.toByteArray(txt.toCharArray(), radix);
        FileIO.dump(exp);
        FileIO.dump(result);
        assertArrayEquals(expResult, result);
    }
    
}
