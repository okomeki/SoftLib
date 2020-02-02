package net.siisise.security;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import net.siisise.io.PacketA;

/**
 *
 */
class SHA512t extends SHA512 {

    static Map<Integer,long[]> hi = new HashMap<>();

    public SHA512t(int n) {
        super(n);
    }
    
    @Override
    protected void engineReset() {
        long[] H0 = hi.get(digestLength);
        if ( H0 == null) {
            SHA512 s5 = new SHA512();
            for ( int i = 0; i < 8; i++) {
                s5.H[i] ^= 0xa5a5a5a5a5a5a5a5l;
            }
            byte[] d = s5.digest(("SHA-512/" + digestLength).getBytes());
            H0 = new long[8];
            for ( int i = 0; i < 8; i++) {
                for ( int j = 0; j < 8; j++) {
                    H0[i] <<= 8;
                    H0[i] |= d[i*8 + j] & 0xff;
                }
            }
            hi.put(digestLength, H0);
        }
        H = new long[8];
        System.arraycopy(H0,0,H,0,8);

        pac = new PacketA();
        length = BigInteger.valueOf(0);
    }
}
