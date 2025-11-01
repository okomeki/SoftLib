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

import net.siisise.lang.Bin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LittleBitPacketTest {
    
    public LittleBitPacketTest() {
    }

    /**
     * Test of readPacket method, of class LittleBitPacket.
     */
    @Test
    public void testReadPacket() {
        System.out.println("readPacket");
        long length = 3L;
        byte[] src = {0,1,2,3,4,5,6,7,8};
        LittleBitPacket instance = new LittleBitPacket();
        instance.write(src);
        byte[] expResult = {0,1,2};
        LittleBitPacket resultPacket = instance.readPacket(length);
        byte[] result = resultPacket.toByteArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of backReadPacket method, of class LittleBitPacket.
     */
    @Test
    public void testBackReadPacket() {
        System.out.println("backReadPacket");
        long length = 3L;
        byte[] src = {0,1,2,3,4,5,6,7,8};
        LittleBitPacket instance = new LittleBitPacket();
        instance.write(src);
        byte[] expResult = {6,7,8};
        LittleBitPacket resultPacket = instance.backReadPacket(length);
        byte[] result = resultPacket.toByteArray();
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testReadBit() {
        System.out.println("readBit");
        LittleBitPacket pac = new LittleBitPacket();
        byte[] bin = Bin.toByteArray("0123456789");
        pac.write(bin);
        byte[] result = new byte[5];
        pac.backReadBit(result, 0, 4);
        System.out.println(Bin.toHex(result));
        pac.backReadBit(result, 8, 2);
        System.out.println(Bin.toHex(result));
        pac.backReadBit(result, 15, 2);
        System.out.println(Bin.toHex(result));
    }

    /**
     * 右詰め.
     * ビット読み方向→
     */
    @Test
    public void testBackRead() {
        System.out.println("backReadBit");
        byte[] exResult = {0x76,0,0,0};
        LittleBitPacket lpac = new LittleBitPacket();
        byte[] bin = Bin.toByteArray("0123456789abcdef");
        lpac.write(bin);
        bin = new byte[4];
        long len = lpac.backReadBit(bin, 1, 6);
        System.out.println(Bin.toHex(bin));
        assertEquals(6, len);
        assertArrayEquals(exResult, bin);
        
    }
}
