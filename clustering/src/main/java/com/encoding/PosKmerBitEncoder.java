package com.encoding;

import com.model.Encoder;

public class PosKmerBitEncoder implements Encoder<String, long[]> {

    private final int w;

    public PosKmerBitEncoder(int w) {
        if (w > 32) throw new RuntimeException("Cannot encode window size bigger than 32bp");
        this.w = w;
    }

    @Override
    public long[] encode(String value) {
        if (value.length() % w != 0) throw new IllegalArgumentException("Read must be divisible by set window size!");
        long[] out = new long[value.length() / w];
        for (int i = 0; i < value.length(); i += w) {
            out[i/w] = encodeSection(value, i, i+w);
        }
        return out;
    }

    private long encodeSection(String value, int start, int end) {
        long res = 0L;
        for (int i = start; i < end; i++) {
            long base = switch (value.charAt(i)) {
                case 'A' -> 0L;
                case 'T' -> 1L;
                case 'C' -> 2L;
                case 'G' -> 3L;
                default -> 0L; // Lol
            };
            res = (res << 2) | base;
        }
        return res;
    }
}
