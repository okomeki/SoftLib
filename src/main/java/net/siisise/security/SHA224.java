package net.siisise.security;

import net.siisise.io.PacketA;

/**
 *
 */
public class SHA224 extends SHA256 {
    
    public static String OBJECTIDENTIFIER = "2.16.840.1.101.3.4.2.4";
    
    public SHA224() {
        super("SHA-224");
    }
    
    @Override
    protected int engineGetDigestLength() {
        return 28;
    }

    @Override
    protected void engineReset() {
        H = new int[]{
            0xc1059ed8,
            0x367cd507,
            0x3070dd17,
            0xf70e5939,
            0xffc00b31,
            0x68581511,
            0x64f98fa7,
            0xbefa4fa4
        };
        pac = new PacketA();
        length = 0;
    }

    @Override
    protected byte[] engineDigest() {

        long len = length;

        // ラスト周
        // padding
        pac.write(new byte[]{(byte) 0x80});
        int padlen = 512 - (int) ((len + 64+8) % 512);
        pac.write(new byte[padlen / 8]);
        byte[] lena = new byte[8];
        for ( int i = 0; i < 8; i++ ) {
            lena[7-i] = (byte) (len & 0xff);
            len >>>= 8;
        }
        engineUpdate(lena,0,lena.length);

        byte[] ret = SHA256.toB(H,7);
        engineReset();
        return ret;
    }
}
