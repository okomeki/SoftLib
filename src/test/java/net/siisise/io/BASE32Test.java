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

import net.siisise.lang.Bin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class BASE32Test {
    
    public BASE32Test() {
    }

    /**
     * Test of decode method, of class BASE32.
     */
    @Test
    public void testDecode() {
        System.out.println("decode Bech32");
        String src = "npub1sfrt23p8xarvfvke4pvez9lh7vgvm8y87k8dppswgv4f5wwgcpsq3s5c38";
        BASE32 instance = new BASE32(BASE32.Bech32);
        String ex = "8246b544273746c4b2d9a8599117f7f310cd9c87f58ed0860e432a9a39c8c060";
        byte[] expResult = Bin.toByteArray(ex);
        byte[] result = instance.decode(src.substring(src.indexOf('1') + 1,src.length() - 6));
        System.out.println(Bin.toHex(result));
        assertArrayEquals(expResult, result);
    }
    
}
