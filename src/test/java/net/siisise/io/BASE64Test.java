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

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class BASE64Test {
    
    public BASE64Test() {
    }

    @Test
    public void testBCryptEncode() {
        System.out.println("encode");
        byte[] src = "spamandeggs".getBytes(StandardCharsets.UTF_8);
        BASE64 instance = new BASE64(BASE64.BCRYPT, 0);
        String expResult = "a1/fZUDsXETlX1K";
        String result = instance.encode(src);
        assertEquals(expResult, result);
    }

    /**
     * Test of decode method, of class BASE64.
     */
    @Test
    public void testBCryptDecode() {
        System.out.println("decode");
        String data = "a1/fZUDsXETlX1K";
        BASE64 instance = new BASE64(BASE64.BCRYPT, 0);
        byte[] expResult = "spamandeggs".getBytes(StandardCharsets.UTF_8);
        byte[] result = instance.decode(data);
        assertArrayEquals(expResult, result);
    }
    
}
