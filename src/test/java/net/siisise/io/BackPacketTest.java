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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Output と RevInput のテストを書いておけばいいかな
 * @author okome
 */
public class BackPacketTest {
    
    public BackPacketTest() {
    }

    @Test
    public void testSimpleFifo() {
        System.out.println("BackPacket fifo test");
        byte[] data = "0123456789".getBytes();
        byte[] buf = new byte[4];
        byte[] exa = "6789".getBytes();
        BackPacket pac = new ByteBlock(new byte[10]);
        pac.write(data);
        pac.backRead(buf);
        assertArrayEquals(exa,buf);
        assertEquals(pac.backLength(),6);
    }
    
}
