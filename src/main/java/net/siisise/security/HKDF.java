package net.siisise.security;

import java.security.MessageDigest;
import net.siisise.io.PacketA;

/**
 * RFC 5869 HKDF.
 * RFC 6234.
 * RFC 8619 HKDFのOID.
 *
 */
public class HKDF {

    private MessageDigest sha;

    public HKDF(MessageDigest sha) {
        this.sha = sha;
    }

    /**
     *
     * @param salt 塩
     * @param ikm 秘密鍵
     * @param info 付加
     * @param length リクエスト鍵長 (HMACの255倍まで)
     * @return
     */
    public byte[] hkdf(byte[] salt, byte[] ikm, byte[] info, int length) {
        byte[] prk = extract(salt, ikm);
        return expand(prk, info, length);
    }

    /**
     *
     * @param salt 塩
     * @param ikm 秘密鍵
     * @return 中間鍵
     */
    byte[] extract( byte[] salt, byte[] ikm) {
        if (salt == null) {
            salt = new byte[0];
        }
        HMAC mac = new HMAC(sha, salt);
        return mac.hmac(ikm);
    }

    /**
     * 鍵長になるまで繰り返し.
     * 
     * @param prk 中間鍵
     * @param info 付加
     * @param length 鍵長
     * @return 
     */
    private byte[] expand(byte[] prk, byte[] info, int length) {
        int l = sha.getDigestLength();
        int n = ((length + l - 1) / l);
        if (info == null) {
            info = new byte[0];
        }
        PacketA pt = new PacketA();
        byte[] t = new byte[0];
        HMAC mac = new HMAC(sha, prk);
        for (int i = 1; i <= n; i++) {
            // d = t(i-1) + info + (byte)i
            byte[] d = new byte[t.length + info.length + 1];
            System.arraycopy(t, 0, d, 0, t.length);
            System.arraycopy(info, 0, d, t.length, info.length);
            d[d.length - 1] = (byte) i;
            t = mac.hmac(d);
            pt.write(t);
        }
        byte[] r = new byte[length];
        pt.read(r);
        return r;
    }
}
