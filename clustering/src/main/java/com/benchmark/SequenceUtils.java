package com.benchmark;

import java.util.Random;

public final class SequenceUtils {

    private static final char[] BASES = {'A', 'C', 'G', 'T'};
    private static final Random RNG = new Random(); // or inject for reproducibility

    /**
     * Generates a random DNA sequence.
     *
     * @param length length of sequence
     * @param nRate  probability of generating 'N' (0.0 to 1.0)
     * @return random DNA sequence containing A,C,G,T and N
     */
    public static String randomSequence(int length, double nRate) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be >= 0");
        }
        if (nRate < 0.0 || nRate > 1.0) {
            throw new IllegalArgumentException("nRate must be between 0.0 and 1.0");
        }

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            if (RNG.nextDouble() < nRate) {
                sb.append('N');
            } else {
                sb.append(BASES[RNG.nextInt(4)]);
            }
        }

        return sb.toString();
    }

    private SequenceUtils() {}
}