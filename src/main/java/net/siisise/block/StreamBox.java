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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.siisise.io.Input;
import net.siisise.lang.Bin;

/**
 * Stream に変換する何か
 */
public class StreamBox {
    
    public static Stream stream(Input in, int len) {
        byte[] d;
        List<byte[]> list = new ArrayList<>();
        
        while ( in.size() >= len ) {
            d = new byte[len];
            in.read(d);
            list.add(d);
        }
        return list.stream();
    }

    /**
     * int列のブロックのストリームを作るかもしれない.
     * @param in
     * @param len intのサイズ
     * @return 
     */
    public static Stream intStream(Input in, int len) {
        List<int[]> list = new ArrayList<>();
        int x4 = len*4;
        
        while ( in.size() >= x4 ) {
            byte[] t = new byte[x4];
            in.read(t);
            list.add(Bin.btoi(t));
        }
        return list.stream();
    }

    public static Stream longStream(Input in, int len) {
        List<long[]> list = new ArrayList<>();
        int x8 = len*8;
        
        while ( in.size() >= x8 ) {
            byte[] t = new byte[x8];
            in.read(t);
            list.add(Bin.btol(t));
        }
        return list.stream();
    }
}
