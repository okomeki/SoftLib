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

import java.io.InputStream;
import net.siisise.block.ReadableBlock;
import net.siisise.lang.Bin;

/**
 * なに?
 * Input と ReadableBlock または RevOutput を持っていると使えそうな 演算系
 */
public interface BinInput extends Input {
    default Input xor(Input in) {
        return new XorInput(this,in);
    }
    /*
    default Input or(Input in) {
        return new OrInput(this,in);
    }
    default Input not(Input in) {
        return new NotInput(this,in);
    }
    default Input and(Input in) {
        return new AndInput(this,in);
    };
*/
    
    class XorInput implements Input {
        BinInput in1;
        Input in2;
        
        XorInput(BinInput in1, Input in2) {
            this.in1 = in1;
            this.in2 = in2;
        }

        @Override
        public InputStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        void back(byte[] a) {
            if ( in1 instanceof ReadableBlock) {
                ((ReadableBlock)in1).back(a.length);
            } else if ( in1 instanceof RevOutput ) {
                ((RevOutput)in1).backWrite(a);
            }
        }

        void back(byte[] a, int of, int len) {
            if ( in1 instanceof ReadableBlock) {
                ((ReadableBlock)in1).back(len);
            } else if ( in1 instanceof RevOutput ) {
                ((RevOutput)in1).backWrite(a, of, len);
            }
        }

        @Override
        public int read() {
            int a = in1.read();
            if ( a < 0 ) return -1;
            int b = in2.read();
            if ( b < 0 ) {
                back(new byte[]{(byte)a});
                return -1;
            }
            return a ^ b;
        }

        @Override
        public int read(byte[] buf, int offset, int length) {
            byte[] tmp1;
            int offset1;
            int len1;
            if ( in1 instanceof ReadableBlock && ((ReadableBlock)in1).hasArray() ) {
                ReadableBlock rb = (ReadableBlock)in1;
                tmp1 = rb.array();
                offset1 = rb.arrayOffset();
                len1 = Math.min(length, rb.size());
                in1.skip(len1);
            } else {
                tmp1 = new byte[length];
                offset1 = 0;
                len1 = in1.read(tmp1);
            }
            if ( len1 < 0 ) {
                return len1;
            }
            byte[] tmp2 = new byte[len1];
            int len2 = in2.read(tmp2, 0, len1);
            if ( len2 < 0 ) {
                back(tmp1,offset1,len1);
                return len2;
            } else if ( len2 < len1 ) {
                back(tmp1, offset1 + len2, len1 - len2);
            }
            Bin.xor(tmp2,0,tmp1,offset1,tmp2,0);
            System.arraycopy(tmp2, 0, buf, offset, len2);
            return len2;
        }

        @Override
        public int read(byte[] d) {
            return read(d,0,d.length);
        }
        
        @Override
        public byte get() {
            if ( size() < 1 ) {
                throw new java.nio.BufferUnderflowException();
            }
            byte a = in1.get();
            byte b = in2.get();
            return (byte)(a ^ b);
        }

        @Override
        public long get(byte[] b, int offset, int length) {
            if ( size() < length ) {
                throw new java.nio.BufferUnderflowException();
            }
            return read(b,offset,length);
        }

        @Override
        public byte[] toByteArray() {
            byte[] tmp = new byte[size()];
            read(tmp);
            return tmp;
        }

        @Override
        public Packet readPacket(long length) {
            Packet pac = new PacketA();
            pac.write(this, length);
            return pac;
        }

        @Override
        public long skip(long length) {
            long l = in1.skip(length);
            return in2.skip(l);
        }

        @Override
        public long length() {
            return Math.min(in1.length(), in2.length());
        }

        @Override
        public int size() {
            return Math.min(in1.size(), in2.size());
        }
    }
}
