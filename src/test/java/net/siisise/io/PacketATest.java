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
     * Test of split method, of class PacketA.
     */
    @Test
    public void testSplit() {
        System.out.println("split");
        byte[] data = "012".getBytes();
        byte[] data2 = "3456789".getBytes();
        int length = 4;
        byte[] expResult = "0123".getBytes();
        PacketA instance = new PacketA(data);
        instance.write(data2);
        PacketA result = instance.split(length);
        System.out.println("result.size " + result.size());
        byte[] tmp = new byte[result.size()];
        System.out.println(tmp.length);
        System.out.println(new String(tmp));
        result.read(tmp);
        assertArrayEquals(expResult, tmp);
    }
    
}
