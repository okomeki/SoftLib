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

import net.siisise.block.ByteBlock;
import net.siisise.block.OverBlock;
import net.siisise.block.PacketBlock;
import net.siisise.block.SinglePacketBlock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author okome
 */
public class FrontPacketTest {
    
    public FrontPacketTest() {
    }

    @Test
    public void testSomeMethod() {
        System.out.println("FrontPacket fifo test");
        byte[] data = "0123456789".getBytes();
        byte[] buf = new byte[4];
        byte[] exa = "0123".getBytes();
//        OverBlock pac = new ByteBlock(new byte[10]);
        OverBlock pac = new PacketBlock(new byte[10]);
//        OverBlock pac = new SinglePacketBlock(new byte[10]);
        pac.write(data);
        assertEquals(pac.length(),0);
        assertEquals(pac.backLength(),10);
        pac.backWrite(data);
        assertEquals(pac.length(),10);
        assertEquals(pac.backLength(),0);
        pac.read(buf);
        assertArrayEquals(exa,buf);
        assertEquals(pac.length(),6);
    }
    
}
