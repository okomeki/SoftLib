package net.siisise.security;

import java.math.BigInteger;
import net.siisise.io.PacketA;

/**
 * RFC 6234
 */
public class SHA384 extends SHA512 {
    
    public static int[] OBJECTIDENTIFIER = {2,16,840,1,101,3,4,2,2};

    public SHA384() {
        super("SHA-384");
    }

    @Override
    protected void engineReset() {
        H = new long[]{
            0xcbbb9d5dc1059ed8l,
            0x629a292a367cd507l,
            0x9159015a3070dd17l,
            0x152fecd8f70e5939l, 
            0x67332667ffc00b31l,
            0x8eb44a8768581511l,
            0xdb0c2e0d64f98fa7l,
            0x47b5481dbefa4fa4l
        };
        pac = new PacketA();
        length = BigInteger.valueOf(0);
    }

    @Override
    protected byte[] engineDigest() {

        BigInteger len = length;
        byte[] lb = len.toByteArray();

        // ラスト周
        // padding
        pac.write(new byte[]{(byte) 0x80});
        int padlen = 1024 - (int) ((len.longValue() + lb.length*8+8) % 512);
        pac.write(new byte[padlen / 8]);

        engineUpdate(lb,0,lb.length);

        long[] h2 = new long[6];
        System.arraycopy(H,0,h2,0,6);
        byte[] ret = toB(h2);
        engineReset();
        return ret;
    }
}
