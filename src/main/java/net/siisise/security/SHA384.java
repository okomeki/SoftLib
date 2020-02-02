package net.siisise.security;

import java.math.BigInteger;
import net.siisise.io.PacketA;

/**
 * RFC 6234
 */
public class SHA384 extends SHA512 {
    
    public static String OBJECTIDENTIFIER = "2.16.840.1.101.3.4.2.2";

    public SHA384() {
        super("SHA-384", 384);
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
}
