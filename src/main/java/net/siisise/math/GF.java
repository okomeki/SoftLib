package net.siisise.math;

/**
 * ガロア体のなにか
 */
public class GF {

    final int N;   // 7
    final int root; // 0x11b
    int size; // 255

    final int[] x;
    final int[] log;
    final int[] exp;

    public GF() {
        this(8, 0x11b);
    }

    /**
     *
     * @param n 2^n bit
     * @param m n = 8のとき 0x11bくらい
     */
    public GF(int n, int m) {
        N = n - 1;
        root = m;
        size = (1 << n) - 1;
        x = new int[size + 1];
        log = new int[size + 1];
        exp = new int[size + 1];

        for (int a = 0; a <= size; a++) {
            x[a] = (a << 1) ^ ((a >>> N) * root);
        }

        int a = 1;
        for (int e = 0; e < size; e++) {
            log[a] = e;
            exp[e] = a;

            a ^= x(a);
        }
        log[0] = 0;
        exp[size] = exp[0];
    }

    public final int x(int a) {
//        return (a << 1) ^ ((a >>> N) * root); 
        return x[a];
    }

    public int inv(int a) {
        return a == 0 ? 0 : exp[size - log[a]];
    }
//*    

    public int mul(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        a &= size;
        b &= size;
        int e = log[a] + log[b];
        if (e >= size) {
            e -= size;
        }
        return exp[e];
    }

/*/
    public int mul(int a, byte bs) {
        if ( a == 0 || b == 0 )
            return 0;
        a &= size;
        int b = bs & size;
        
        int e = log[a] + log[b];
        if ( e >= size ) {
            e -= size;
        }
        return exp[e];
    }
//*/
    /*
    public int mul(int x, int y) {
        int m = 0;
        x &= size;
        y &= size;
        while (x > 0) {
            if ((x & 1) != 0) {
                m ^= y;
            }
            y = x(y);
            x >>>= 1;
        }
        return m;
    }
*/
    public int div(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        int e = log[a] - log[b];
        if (e < 0) {
            e += size;
        }
        return exp[e];
    }

}
