/*
 * Copyright 2024 okome.
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
package net.siisise.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class BigBitPacketTest {
    
    public BigBitPacketTest() {
    }

    /**
     * Test of del method, of class BigBitPacket.
     */
    @Test
    public void testDel() {
        System.out.println("del");
        long index = 4L;
        byte[] ex = {0,1,2,3,4,5,6};
        byte[] buf = new byte[] {1,2,3,4};
        byte[] expBuf = {1,4,5,4};
        long exBitLen = 42;
        int offset = 1;
        int length = 2;
        BigBitPacket instance = new BigBitPacket();
        instance.write(ex);
        instance.writeBit(3,2);
        
        BigBitPacket expResult = instance;
        BaseBitPac result = instance.del(index, buf, offset, length);
        assertEquals(expResult, result);
        assertArrayEquals(expBuf, buf);
        long len = instance.bitLength();
        assertEquals(exBitLen, len);
    }

    /**
     * Test of toString method, of class BigBitPacket.
     */
/*
    @Test
    public void testToString() {
        System.out.println("toString");
        BigBitPacket instance = new BigBitPacket();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/

    /**
     * Test of readPacket method, of class BigBitPacket.
     */
    @Test
    public void testReadPacket() {
        System.out.println("readPacket");
        long length = 3L;
        byte[] src = {0,1,2,3,4,5,6,7,8};
        BigBitPacket instance = new BigBitPacket();
        instance.write(src);
        byte[] expResult = {0,1,2};
        BigBitPacket resultPacket = instance.readPacket(length);
        byte[] result = resultPacket.toByteArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of backReadPacket method, of class BigBitPacket.
     */
    @Test
    public void testBackReadPacket() {
        System.out.println("backReadPacket");
        long length = 3L;
        byte[] src = {0,1,2,3,4,5,6,7,8};
        BigBitPacket instance = new BigBitPacket();
        instance.write(src);
        byte[] expResult = {6,7,8};
        BigBitPacket resultPacket = instance.backReadPacket(length);
        byte[] result = resultPacket.toByteArray();
        assertArrayEquals(expResult, result);
    }
}
