package me.alphamode;

import java.util.Random;
import java.util.UUID;

public class MathHelper {
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{
            0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
    };
    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];

    public static double atan2(double d, double e) {
        double f = e * e + d * d;
        if (Double.isNaN(f)) {
            return Double.NaN;
        } else {
            boolean bl = d < 0.0;
            if (bl) {
                d = -d;
            }

            boolean bl2 = e < 0.0;
            if (bl2) {
                e = -e;
            }

            boolean bl3 = d > e;
            if (bl3) {
                double g = e;
                e = d;
                d = g;
            }

            double g = fastInvSqrt(f);
            e *= g;
            d *= g;
            double h = FRAC_BIAS + d;
            int i = (int)Double.doubleToRawLongBits(h);
            double j = ASIN_TAB[i];
            double k = COS_TAB[i];
            double l = h - FRAC_BIAS;
            double m = d * k - e * l;
            double n = (6.0 + m * m) * m * 0.16666666666666666;
            double o = j + n;
            if (bl3) {
                o = (Math.PI / 2) - o;
            }

            if (bl2) {
                o = Math.PI - o;
            }

            if (bl) {
                o = -o;
            }

            return o;
        }
    }

    public static double fastInvSqrt(double d) {
        double e = 0.5 * d;
        long l = Double.doubleToRawLongBits(d);
        l = 6910469410427058090L - (l >> 1);
        d = Double.longBitsToDouble(l);
        return d * (1.5 - e * d * d);
    }

    public static float wrapDegrees(float f) {
        float g = f % 360.0F;
        if (g >= 180.0F) {
            g -= 360.0F;
        }

        if (g < -180.0F) {
            g += 360.0F;
        }

        return g;
    }

    public static double wrapDegrees(double d) {
        double e = d % 360.0;
        if (e >= 180.0) {
            e -= 360.0;
        }

        if (e < -180.0) {
            e += 360.0;
        }

        return e;
    }

    public static float clamp(float f, float g, float h) {
        if (f < g) {
            return g;
        } else {
            return f > h ? h : f;
        }
    }

    public static int clamp(int i, int j, int k) {
        if (i < j) {
            return j;
        } else {
            return i > k ? k : i;
        }
    }

    public static double clamp(double d, double e, double f) {
        if (d < e) {
            return e;
        } else {
            return d > f ? f : d;
        }
    }

    public static double clampedLerp(double d, double e, double f) {
        if (f < 0.0) {
            return d;
        } else {
            return f > 1.0 ? e : lerp(f, d, e);
        }
    }

    public static float lerp(float f, float g, float h) {
        return g + f * (h - g);
    }

    public static double lerp(double d, double e, double f) {
        return e + d * (f - e);
    }

    public static double lerp2(double d, double e, double f, double g, double h, double i) {
        return lerp(e, lerp(d, f, g), lerp(d, h, i));
    }

    public static double lerp3(double d, double e, double f, double g, double h, double i, double j, double k, double l, double m, double n) {
        return lerp(f, lerp2(d, e, g, h, i, j), lerp2(d, e, k, l, m, n));
    }

    public static double smoothstep(double d) {
        return d * d * d * (d * (d * 6.0 - 15.0) + 10.0);
    }

    public static UUID createInsecureUUID(Random random) {
        long l = random.nextLong() & -61441L | 16384L;
        long m = random.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID(l, m);
    }

    public static UUID createInsecureUUID() {
        return createInsecureUUID(RANDOM);
    }

    public static int smallestEncompassingPowerOfTwo(int i) {
        int j = i - 1;
        j |= j >> 1;
        j |= j >> 2;
        j |= j >> 4;
        j |= j >> 8;
        j |= j >> 16;
        return j + 1;
    }

    private static boolean isPowerOfTwo(int i) {
        return i != 0 && (i & i - 1) == 0;
    }

    public static int ceillog2(int i) {
        i = isPowerOfTwo(i) ? i : smallestEncompassingPowerOfTwo(i);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)i * 125613361L >> 27) & 31];
    }

    public static int log2(int i) {
        return ceillog2(i) - (isPowerOfTwo(i) ? 0 : 1);
    }

    static {
        for(int i = 0; i < 257; ++i) {
            double d = (double)i / 256.0;
            double e = Math.asin(d);
            COS_TAB[i] = Math.cos(e);
            ASIN_TAB[i] = e;
        }
    }
}
