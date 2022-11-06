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

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ByteBlockTest {
    
    public ByteBlockTest() {
    }

    /**
     * Test of readBlock method, of class ByteBlock.
     */
    @Test
    public void testReadBlock() {
        System.out.println("readBlock");
        byte[] ss = "utca".getBytes(StandardCharsets.UTF_8);
        ByteBlock block = new ByteBlock(ss);
        byte[] rd = new byte[1];
        
        assertEquals(4,block.size());
        assertEquals(4,block.length());
        
        block.read(rd);
        
        assertEquals(3,block.size());
        assertEquals(3,block.length());
        
        ByteBlock nb = block.readBlock(1);

        assertEquals(2,block.size());
        assertEquals(2,block.length());
        assertEquals(2,block.backSize());
        
        assertEquals(1,nb.size());
        assertEquals(1,nb.length());
        assertEquals(0,nb.backSize());
    }
}
