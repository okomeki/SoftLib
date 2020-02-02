package net.siisise.security;

import java.security.MessageDigest;

/**
 * Java標準ではない仮の鍵付きハッシュの形.
 * あとで標準に寄せる。
 * 
 * H 暗号ハッシュ関数.
 * K 秘密鍵 / 認証鍵.
 * B Hのブロックバイト長 512 / 8
 * L ハッシュバイト長 (MD5:128/8 SHA-1:160/8)
 * ipad 0x36をB回繰り返したもの
 * opad 0x5c をB回繰り返したもの
 *
 * MD5 B 512bit L 128bit
 * 
 * RFC 2104 HMAC: Keyed-Hashing for Message Authentication.
 * RFC 2202 テスト
 */
public class HMAC {
    
    public static final String rsadsi = "1.2.840.113549";
    public static final String digestAlgorithm = rsadsi + ".2";
    public static final String idhmacWithSHA224 = digestAlgorithm + ".8";
    public static final String idhmacWithSHA256 = digestAlgorithm + ".9";
    public static final String idhmacWithSHA384 = digestAlgorithm + ".10";
    public static final String idhmacWithSHA512 = digestAlgorithm + ".11";

    private MessageDigest md;
    private byte[] k_ipad;
    private byte[] k_opad;

    /**
     *
     * @param md MD5, SHA-1, SHA-256 など512bitブロックのもの
     * @param key 鍵
     */
    public HMAC(MessageDigest md, byte[] key) {
        this.md = md;
        genPad(key, 512);
    }

    public HMAC(MessageDigest md, int blockBitLength, byte[] key) {
        this.md = md;
        genPad(key, blockBitLength);
    }

    /**
     * 鍵.
     * L以上の長さが必要.
     * B以上の場合はハッシュ値に置き換える.
     * @param key 鍵
     */
    void genPad(byte[] key, int blockLength) {
        int b = blockLength / 8;
        md.reset();
        if ( key.length > b) {
            key = md.digest(key);
        }
        
        k_ipad = new byte[b];
        k_opad = new byte[b];

        System.arraycopy(key, 0, k_ipad, 0, key.length);
        System.arraycopy(key, 0, k_opad, 0, key.length);

        for (int i = 0; i < b; i++) {
            k_ipad[i] ^= 0x36;
            k_opad[i] ^= 0x5c;
        }
        md.update(k_ipad);
    }
    
    public void update(byte[] src) {
        md.update(src);
    }

    public void update(byte[] src, int offset, int len) {
        md.update(src, offset, len);
    }

    /**
     *
     * @param src
     * @return HMAC値
     */
    public byte[] hmac(byte[] src) {
        byte[] m = md.digest(src);

        md.update(k_opad);
        byte[] r = md.digest(m);
        md.update(k_ipad);
        return r;
    }

}
