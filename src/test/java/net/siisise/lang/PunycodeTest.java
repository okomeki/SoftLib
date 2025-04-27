/*
 * Copyright 2025 okome.
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
package net.siisise.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Punycode test
 */
public class PunycodeTest {
    
    public PunycodeTest() {
    }

    /**
     * Test of toASCII method, of class Punycode.
     */
    @Test
    public void testToASCII() {
        System.out.println("toASCII");
        java.lang.String u = "3年B組金八先生";
        java.lang.String expResult = "3B-ww4c5e180e575a65lsy2b";
        java.lang.String result = Punycode.toASCII(u);
        assertEquals(expResult, result);
        
        u = "\u0644\u064a\u0647\u0645\u0627\u0628\u062A\u0643\u0644\u0645\u0648\u0634\u0639\u0631\u0628\u064A\u061F";
        expResult = "egbpdaj6bu4bxfgehfvwxn";
        result = Punycode.toASCII(u);
        assertEquals(expResult, result);

        u = "\u4ED6\u4EEC\u4E3A\u4EC0\u4E48\u4E0D\u8BF4\u4E2D\u6587";
        expResult = "ihqwcrb4cv8a8dqg056pqjye";
        result = Punycode.toASCII(u);
        assertEquals(expResult, result);

        u = "安室奈美恵-with-SUPER-MONKEYS";
        expResult = "-with-SUPER-MONKEYS-pc58ag80a8qai00g7n9n";
        result = Punycode.toASCII(u);
        assertEquals(expResult, result);
    }

    /**
     * Test of toUnicode method, of class Punycode.
     */
    @Test
    public void testToUnicode() {
        System.out.println("toUnicode");
        java.lang.String a = "3B-ww4c5e180e575a65lsy2b";
        java.lang.String expResult = "3年B組金八先生";
        java.lang.String result = Punycode.toUnicode(a);
        assertEquals(expResult, result);
    }
    
}
