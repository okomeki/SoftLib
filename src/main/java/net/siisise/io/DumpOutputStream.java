/*
 * Copyright 2003-2022 Siisise Net.
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
 *
 * Created on 2003/05/09, 7:16
 */
package net.siisise.io;

import java.io.*;

/**
 * ファイルダンプ形式で出力する。
 */
public class DumpOutputStream extends OutputStream {

    int cnt;
    PrintWriter out;
    int xsum;
    int[] ysum;

    /**
     * Creates a new instance of DumpOutputStream
     *
     * @param w
     */
    public DumpOutputStream(PrintWriter w) {
        cnt = 0;
        this.out = w;
    }

    public DumpOutputStream(Writer out) {
        cnt = 0;
        this.out = new PrintWriter(out);
    }

    @Override
    public void write(int d) throws IOException {
        trace((byte) d);
    }

    @Override
    public void write(byte dt[], int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            trace(dt[i]);
        }
    }

    /**
     *
     */
    void trace(byte r) {
        String addr;
        if (cnt % 256 == 0) {
            out.println("ADDRESS : +0 +1 +2 +3 +4 +5 +6 +7 +8 +9 +A +B +C +D +E +F Sum");
            ysum = new int[16];
        }
        if (cnt % 16 == 0) {
            addr = "0000000" + Integer.toHexString(cnt);
            out.print(addr.substring(addr.length() - 8) + ": ");
            xsum = 0;
        }
        String hex = "0" + Integer.toHexString(r);
        xsum += r;
        ysum[cnt % 16] += r;
        out.write(hex.substring(hex.length() - 2) + ' ');
        if (cnt % 16 == 15) {
            addr = "0" + Integer.toHexString(xsum);
            out.println(":" + addr.substring(addr.length() - 2));
            out.flush();
        }
        if (cnt % 256 == 255) {
            out.println("-------------------------------------------------------------");
            out.print("CheckSum:");
            xsum = 0;
            for (int i = 0; i < 16; i++) {
                addr = "0" + Integer.toHexString(ysum[i]);
                xsum += ysum[i];
                out.print(" " + addr.substring(addr.length() - 2));
            }
            addr = "0" + Integer.toHexString(xsum);
            out.println(" :" + addr.substring(addr.length() - 2));
            out.println();
            out.flush();
        }
        cnt++;
    }

    @Override
    public void flush() {
        out.flush();
    }

    @Override
    public void close() {
        out.close();
    }
}
