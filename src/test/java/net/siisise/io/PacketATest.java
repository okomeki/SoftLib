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
package net.siisise.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class PacketATest {
    
    public PacketATest() {
    }

    /**
     * Test of readPacket method, of class PacketA.
     */
    @Test
    public void testReadPacket() {
        System.out.println("split");
        byte[] data = "012".getBytes();
        byte[] data2 = "3456789".getBytes();
        int length = 4;
        byte[] expResult = "0123".getBytes();
        PacketA instance = new PacketA(data);
        instance.write(data2);
        PacketA result = instance.readPacket(length);
        System.out.println("result.size " + result.size());
        byte[] tmp = new byte[result.size()];
        System.out.println(tmp.length);
        System.out.println(new String(tmp));
        result.read(tmp);
        assertArrayEquals(expResult, tmp);
    }
    
    @Test
    public void testBackReadPacket() {
        System.out.println("backSplit");
        byte[] data = "0123456".getBytes();
        byte[] data2 = "789".getBytes();
        int length = 4;
        byte[] expResult = "6789".getBytes();
        PacketA instance = new PacketA(data);
        instance.write(data2);
        PacketA result = instance.backReadPacket(length);
        System.out.println("result.size " + result.size());
        byte[] tmp = new byte[result.size()];
        System.out.println(tmp.length);
        System.out.println(new String(tmp));
        result.read(tmp);
        assertArrayEquals(expResult, tmp);
        
    }
    
    @Test
    public void testIndex() {
        System.out.println("index");
        byte[] data = "012".getBytes();
        byte[] data2 = "3456789".getBytes();
        byte[] exResult = "0123456789".getBytes();
        PacketA instance = new PacketA(data);
        instance.write(data2);
        byte[] d = new byte[2];
        instance.get(4,d,0,2);
        byte[] tmp = instance.toByteArray();
        assertArrayEquals(exResult,tmp);
        
    }
    
    @Test
    public void testBackWrite() {
        System.out.println("backWrite");
        byte[] data = "012".getBytes();
        byte[] data2 = "3456789".getBytes();
        PacketA pac = new PacketA();
        pac.write(data);
        PacketA pac2 = new PacketA();
        pac2.write(data2);
        pac2.backWrite(pac);
        byte[] expResult = "0123456789".getBytes();
        assertArrayEquals(expResult, pac2.toByteArray());
    }
    
}
