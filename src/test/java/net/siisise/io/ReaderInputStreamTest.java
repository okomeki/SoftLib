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

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ReaderInputStreamTest {
    
    public ReaderInputStreamTest() {
    }

    /**
     * Test of read method, of class ReaderInputStream.
     * @throws java.lang.Exception
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        StringReader src = new StringReader("abcdefg");
        ReaderInputStream instance = new ReaderInputStream(src);
        int expResult = 'a';
        int result = instance.read();
        assertEquals(expResult, result);
    }


    /**
     * Test of available method, of class ReaderInputStream.
     * @throws java.lang.Exception
     */
    @Test
    public void testAvailable() throws Exception {
        System.out.println("available");
        StringReader src = new StringReader("0123456789");
        ReaderInputStream instance = new ReaderInputStream(src);
        instance.read();
        int expResult = 9;
        int result = instance.available();
        assertEquals(expResult, result);
    }
}
