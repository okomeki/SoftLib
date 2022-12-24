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
public class BinTest {
    
    public BinTest() {
    }

    /**
     * Test of toHex method, of class Hex.
     */
    @org.junit.jupiter.api.Test
    public void testToHex() {
        System.out.println("toHex");
        byte[] src = new byte[] {0x01,0x2f,(byte)0xe3,(byte)0x80};
        java.lang.String expResult = "012fe380";
        java.lang.String result = Bin.toHex(src);
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
        java.lang.String result = Bin.toUpperHex(src);
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
        byte[] result = Bin.toByteArray(src);
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
        byte[] result = Bin.toByteArray(src);
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
        byte[] result = Bin.toByteArray(txt);
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
        byte[] result = Bin.toByteArray(txt, offset, length);
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
        System.out.println(Bin.toHex(exp));
        int length = exp.length;
        byte[] expResult = exp;
        byte[] result = Bin.toByteArray(num, length);
        assertArrayEquals(expResult, result);

        exp = new byte[] {(byte)0xff, (byte)0xff, 0x01,0x2f,(byte)0xe5,0x67};
        num = new BigInteger(exp);
        System.out.println(num.toString());
        System.out.println(num.toString(16));
        System.out.println(Bin.toHex(exp));
        length = exp.length;
        expResult = exp;
        result = Bin.toByteArray(num, length);
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
        byte[] result = Bin.toByteArray(txt.toCharArray(), radix);
        FileIO.dump(exp);
        FileIO.dump(result);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByte method, of class Bin.
     */
    @Test
    public void testToByte_short() {
        System.out.println("toByte");
        short i = 0x1234;
        byte[] expResult = new byte[] {0x12,0x34};
        byte[] result = Bin.toByte(i);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByte method, of class Bin.
     */
    @Test
    public void testToByte_3args_1() {
        System.out.println("toByte");
        short i = 0x2345;
        byte[] out = new byte[2];
        int offset = 0;
        byte[] expResult = new byte[] {0x23,0x45};
        byte[] result = Bin.toByte(i, out, offset);
        assertArrayEquals(expResult, out);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByte method, of class Bin.
     */
    @Test
    public void testToByte_int() {
        System.out.println("toByte");
        int i = 0x12345678;
        byte[] expResult = new byte[] {0x12,0x34,0x56,0x78};
        byte[] result = Bin.toByte(i);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByte method, of class Bin.
     */
    @Test
    public void testToByte_3args_2() {
        System.out.println("toByte");
        int i = 0x23456789;
        byte[] out = new byte[4];
        int offset = 0;
        byte[] expResult = new byte[] {0x23,0x45,0x67,(byte)0x89};
        byte[] result = Bin.toByte(i, out, offset);
        assertArrayEquals(expResult, out);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByte method, of class Bin.
     */
    @Test
    public void testToByte_long() {
        System.out.println("toByte");
        long l = 0x789abcdef0123456L;
        byte[] expResult = new byte[] {0x78,(byte)0x9a,(byte)0xbc,(byte)0xde,(byte)0xf0,0x12,0x34,0x56};
        byte[] result = Bin.toByte(l);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toByte method, of class Bin.
     */
    @Test
    public void testToByte_3args_3() {
        System.out.println("toByte");
        long l = 0xabcdef0123456789L;
        byte[] out = new byte[8];
        int offset = 0;
        byte[] expResult = new byte[] {(byte)0xab,(byte)0xcd,(byte)0xef,0x01,0x23,0x45,0x67,(byte)0x89};
        byte[] result = Bin.toByte(l, out, offset);
        assertArrayEquals(expResult, out);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of and method, of class Bin.
     */
    @Test
    public void testAnd() {
        System.out.println("and");
        byte[] a = {0x01,0x22,0x03,0x44};
        byte[] b = {0x11,0x02,0x33,0x04};
        byte[] expResult = {0x01,0x02,0x03,0x04};
        byte[] result = Bin.and(a, b);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of or method, of class Bin.
     */
    @Test
    public void testOr() {
        System.out.println("or");
        byte[] a = new byte[] {0x01,0x02,0x03,0x04};
        byte[] b = new byte[] {0x10,0x20,0x30,0x44};
        byte[] expResult = {0x11,0x22,0x33,0x44};
        byte[] result = Bin.or(a, b);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of xor method, of class Bin.
     */
    @Test
    public void testXor() {
        System.out.println("xor");
        byte[] a = new byte[] {0x01,0x02,0x03,0x04};
        byte[] b = new byte[] {0x10,0x20,0x30,0x44};
        byte[] expResult = {0x11,0x22,0x33,0x40};
        byte[] result = Bin.xor(a, b);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of shl method, of class Bin.
     */
    @Test
    public void testShl() {
        System.out.println("shl");
        byte[] a = {0x01,0x02,(byte)0xf3,(byte)0xe4,0x75,0x63};
        byte[] expResult = {0x02,0x05,(byte)0xe7,(byte)0xc8,(byte)0xea,(byte)0xc6};
        byte[] result = Bin.shl(a);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of rol method, of class Bin.
     */
    @Test
    public void testRol() {
        System.out.println("rol");
        byte[] a = {(byte)0x81,0x02,(byte)0xf3,(byte)0xe4,0x75,0x63};
        byte[] expResult = {0x02,0x05,(byte)0xe7,(byte)0xc8,(byte)0xea,(byte)0xc7};
        byte[] result = Bin.rol(a);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of shr method, of class Bin.
     */
    @Test
    public void testShr() {
        System.out.println("shr");
        byte[] a = {0x02,0x05,(byte)0xe7,(byte)0xc8,(byte)0xea,(byte)0xc6};
        byte[] expResult = {0x01,0x02,(byte)0xf3,(byte)0xe4,0x75,0x63};
        byte[] result = Bin.shr(a);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of ror method, of class Bin.
     */
    @Test
    public void testRor() {
        System.out.println("ror");
        byte[] a = {0x02,0x05,(byte)0xe7,(byte)0xc8,(byte)0xea,(byte)0xc7};
        byte[] expResult = {(byte)0x81,0x02,(byte)0xf3,(byte)0xe4,0x75,0x63};
        byte[] result = Bin.ror(a);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of left method, of class Bin.
     */
    @Test
    public void testLeft() {
        System.out.println("left");
        byte[] a = {0x01,0x02,(byte)0xf3,(byte)0xe4,0x75,0x63};
        byte[] expResult = Bin.shl(new byte[] {0x02,0x05,(byte)0xe7,(byte)0xc8,(byte)0xea,(byte)0xc6});
        int shift = 2;
        byte[] result = Bin.left(a, shift);
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of left method, of class Bin.
     */
    @Test
    public void testRight() {
        System.out.println("right");
        byte[] a = {0x02,0x05,(byte)0xe7,(byte)0xc8,(byte)0xea,(byte)0xc6};
        byte[] expResult = Bin.shr(new byte[] {0x01,0x02,(byte)0xf3,(byte)0xe4,0x75,0x63});
        int shift = 2;
        byte[] result = Bin.right(a, shift);
        assertArrayEquals(expResult, result);
    }
}
