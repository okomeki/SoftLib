/*
 * Copyright 2023 okome.
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

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class BASE58Test {
    
    public BASE58Test() {
    }

    /**
     * Test of encode method, of class BASE58.
     */
    @Test
    public void testEncode() {
        System.out.println("encode");
        byte[] bytes = "hello world".getBytes(StandardCharsets.UTF_8);
        int offset = 0;
        int length = bytes.length;
        BASE58 instance = new BASE58();
        String expResult = "StV1DL6CwTryKyV";
//                         "TcgsESe9XJSrakNTEQQ";
        String result = instance.encode(bytes, offset, length);
        
        System.out.println(new String(instance.decode(result), StandardCharsets.UTF_8));
        assertEquals(expResult, result);
    }

    /**
     * Test of decode method, of class BASE58.
     */
    @Test
    public void testDecode() {
        System.out.println("decode");
        String encoded = "StV1DL6CwTryKyV";
        BASE58 instance = new BASE58();
        byte[] expResult = "hello world".getBytes(StandardCharsets.UTF_8);
        byte[] result = instance.decode(encoded);
        assertArrayEquals(expResult, result);
    }
    
}
