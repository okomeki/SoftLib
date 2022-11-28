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
 */
public class FrontPacketTest {

    public FrontPacketTest() {
    }

    /**
     * 書ける状態ですたーと
     *
     * @param tg
     */
    static void frontPacket(FrontPacket tg) {
        System.out.println("FrontPacket fifo test");
        byte[] data = "0123456789".getBytes();
        byte[] buf = new byte[4];
        byte[] exa = "0123".getBytes();
        byte[] exb = "7893".getBytes();
        assertEquals(tg.length(), 0);
        tg.backWrite(data);
        assertEquals(tg.length(), 10);
        tg.read(buf);
        assertArrayEquals(exa, buf);
        assertEquals(tg.length(), 6);
        tg.skip(3);
        int len = tg.read(buf); // 残り3 読み4
        assertArrayEquals(exb, buf);
        assertEquals(len, 3);
        tg.backWrite(data);
//        tg.backWrite(buf);
    }

    @Test
    public void testSomeMethod() {
        System.out.println("FrontPacket fifo test");

        OverBlock block = new ByteBlock(new byte[10]);
        assertEquals(block.length(), 10);
        block.skip(block.length());
        frontPacket(block);
        block = new PacketBlock(new byte[10]);
        assertEquals(block.length(), 10);
        block.skip(block.length());
        frontPacket(block);
        block = new SinglePacketBlock(new byte[10]);
        assertEquals(block.length(), 10);
        block.skip(block.length());
        frontPacket(block);
        FrontPacket pac = new PacketA();
        frontPacket(pac);
    }

}
