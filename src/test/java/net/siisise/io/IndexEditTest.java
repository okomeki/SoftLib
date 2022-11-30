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

import net.siisise.block.EditBlock;
import net.siisise.block.SinglePacketBlock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class IndexEditTest {
    
    public IndexEditTest() {
    }
    
    EditBlock block(byte[] ar) {
        return new SinglePacketBlock(ar);
    }

    /**
     * Test of del method, of class IndexEdit.
     */
    @Test
    public void testDel_long() {
        System.out.println("del");
        long index = 2L;
        IndexEdit instance = block(new byte[] {6,5,4,3,2,1});
        byte expResult = 4;
        byte result = instance.del(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of del method, of class IndexEdit.
     */
    @Test
    public void testDel_long_long() {
        System.out.println("del");
        long index = 2L;
        long size = 3L;
        EditBlock instance = block(new byte[] {5,6,7,8,9,10});
        instance.del(index, size);
        assertEquals(0, instance.backLength() );
        assertEquals(3, instance.length() );
        byte[] exp = new byte[] {5,6,10};
        assertArrayEquals(exp, instance.toByteArray());
    }

    /**
     * Test of del method, of class IndexEdit.
     */
    @Test
    public void testDel_long_byteArr() {
        System.out.println("del");
        long index = 1L;
        byte[] buf = new byte[3];
        EditBlock instance = block(new byte[] {5,6,7,8,9,10});
        byte[] expResult = new byte[] {6,7,8};
        EditBlock result = instance.del(index, buf);
        assertArrayEquals(expResult, buf);
        assertEquals(3,result.length());
    }

    /**
     * Test of del method, of class IndexEdit.
     */
    @Test
    public void testDel_4args() {
        System.out.println("del");
        long index = 3L;
        byte[] buf = new byte[4];
        int offset = 1;
        int length = 2;
        EditBlock instance = block(new byte[] {5,6,7,8,9,10});
        byte[] expResult = new byte[] {0,8,9,0};
        EditBlock result = instance.del(index, buf, offset, length);
        assertEquals(result, instance);
        assertArrayEquals(expResult, buf);
    }

    /**
     * Test of add method, of class IndexEdit.
     */
    @Test
    public void testAdd_long_byte() {
        System.out.println("add");
        long index = 2L;
        byte src = 11;
        byte[] exr = new byte[] {5,6,11,7,8,9,10};
        EditBlock instance = block(new byte[] {5,6,7,8,9,10});
        instance.add(index, src);
        assertArrayEquals(exr, instance.toByteArray());
    }

    /**
     * Test of add method, of class IndexEdit.
     */
    @Test
    public void testAdd_long_byteArr() {
        System.out.println("add");
        long index = 4L;
        byte[] src = new byte[] {0,1,2};
        byte[] exr = new byte[] {5,6,7,8,0,1,2,9,10};
        EditBlock instance = block(new byte[] {5,6,7,8,9,10});
        instance.add(index, src);
        assertArrayEquals(exr, instance.toByteArray());
    }

    /**
     * Test of add method, of class IndexEdit.
     */
    @Test
    public void testAdd_4args() {
        System.out.println("add");
        long index = 4L;
        byte[] src = new byte[] {9,8,7,6};
        int offset = 1;
        int length = 2;
        EditBlock instance = block(new byte[] {5,6,7,8,9,10});
        instance.add(index, src, offset, length);
        byte[] exr = new byte[] {5,6,7,8,8,7,9,10};
        assertArrayEquals(exr, instance.toByteArray());
    }

}
