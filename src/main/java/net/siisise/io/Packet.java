/*
 * Copyright 2021 Siisise Net.
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

/**
 * First In First Out Stream Packet.
 * 配列のような固定長でもなく、リストのような細切れでもないものを作ってみた。
 * メモリとストレージの中間を狙ったようなそうでもないような Bufferの可変長。
 * PipedInputStream / PipedOutputStream のような延長線。
 * 逆向きの読み書きが可能。中間の読み書きは使わないのでやめた。
 *
 * @since JDK 1.1
 */
public interface Packet extends FrontPacket,BackPacket,IndexEdit,BinInput {
}
