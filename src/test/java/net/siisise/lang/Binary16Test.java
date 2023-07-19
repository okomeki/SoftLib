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
package net.siisise.lang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class Binary16Test {
    
    public Binary16Test() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of shortBitsToBinary16 method, of class Binary16.
     */
    @Test
    public void testShortBitsToBinary16() {
        System.out.println("shortBitsToBinary16");
        short sh = 0;
        Binary16 expResult = new Binary16((short)0);
        Binary16 result = Binary16.valueOf(sh);
        assertEquals(expResult, result);
    }

    /**
     * Test of binary16ToFloat method, of class Binary16.
     */
    @Test
    public void testBinary16BitsToFloat() {
        System.out.println("binary16BitsToFloat");
        short binary16 = 0;
        float expResult = 0.0F;
        float result = Binary16.binary16BitsToFloat(binary16);
        assertEquals(expResult, result, 0);
    }
/*
    @Test
    public void ex() {
        System.out.println("Floatの値");
        float f5 = 0.5f;
        int f5i = Float.floatToIntBits(f5);
        System.out.println("0.5   " + Integer.toBinaryString(f5i));
        System.out.print("0.5 e " + Integer.toBinaryString(f5i >>> 23));
        System.out.println(" " + (f5i >>> 23));
        System.out.println("0.5 f " + Integer.toBinaryString(f5i & ((1 << 23) - 1)));
        System.out.println("0.5   " + Integer.toBinaryString(Float.floatToRawIntBits(f5)));

        f5 = 1.0f;
        System.out.println("1.0   " + Integer.toBinaryString(f5i));
        System.out.println("1.0 e " + Integer.toBinaryString(f5i >>> 23));
        System.out.println("1.0 f " + Integer.toBinaryString(f5i & ((1 << 23) - 1)));
        System.out.println("1.0   " + Integer.toBinaryString(Float.floatToRawIntBits(f5)));
        
    }
*/
}
