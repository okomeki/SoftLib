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

import java.math.BigInteger;
import net.siisise.block.ReadableBlock;
import net.siisise.io.Input;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LEB128Test {
    
    public LEB128Test() {
    }

    /**
     * Test of toLEB128 method, of class LEB128.
     */
    @Test
    public void testToLEB128_BigInteger() {
        System.out.println("toLEB128");
        BigInteger val = BigInteger.valueOf(-123456);
        byte[] expResult = {(byte)0xc0,(byte)0xbb,0x78};
        byte[] result = LEB128.toLEB128(val);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toBigInteger method, of class LEB128.
     */
    @Test
    public void testToBigInteger_byteArr() {
        System.out.println("toBigInteger");
        byte[] val = {-1,1};
        BigInteger expResult = BigInteger.valueOf(0xff);
        BigInteger result = LEB128.toBigInteger(val);
        assertEquals(expResult, result);
    }

    /**
     * Test of toBigInteger method, of class LEB128.
     */
    @Test
    public void testToBigInteger_Input() {
        System.out.println("toBigInteger");
        byte[] data = {7};
        Input in = ReadableBlock.wrap(data);
        BigInteger expResult = BigInteger.valueOf(7);
        BigInteger result = LEB128.toBigInteger(in);
        assertEquals(expResult, result);
    }

    /**
     * Test of toLong method, of class LEB128.
     */
    @Test
    public void testToLong() {
        System.out.println("toLong");
        byte[] data = {4};
        Input in = ReadableBlock.wrap(data);
        long expResult = 4L;
        long result = LEB128.toLong(in);
        assertEquals(expResult, result);
    }

    /**
     * Test of toLEB128 method, of class LEB128.
     */
    @Test
    public void testToLEB128_long() {
        System.out.println("toLEB128");
        long sign = 0L;
        byte[] expResult = {0};
        byte[] result = LEB128.toLEB128(sign);
        assertArrayEquals(expResult, result);
    }
    
}
