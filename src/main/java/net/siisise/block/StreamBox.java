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

/**
 * Stream に変換する何か
 */
public class StreamBox {
    
    public static Stream stream(Input in, int len) {
        byte[] d;
        List<byte[]> list = new ArrayList<>();
        
        
        while( in.size() >= len ) {
            d = new byte[len];
            in.read(d);
            list.add(d);
        }
        return list.stream();
    }
}
