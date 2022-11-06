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
package net.siisise.block;

import net.siisise.io.PacketA;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class PacketBlockTest {
    
    public PacketBlockTest() {
    }

    /**
     * Test of skip method, of class PacketBlock.
     */
    @Test
    public void testSkip() {
        System.out.println("skip");
        
        byte[] data = "0123456789".getBytes();
        int length = 4;
        long expResult = 4;

        PacketA p = new PacketA(data);
        PacketA b = p.split(length);
        assertEquals(expResult, b.size());
        
        PacketBlock instance = new PacketBlock(data);
        long result = instance.skip(length);
        assertEquals(expResult, result);
    }
}
