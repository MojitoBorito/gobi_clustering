package com.kmer;

import java.util.Arrays;

public class KmerLongSet implements KmerSet<KmerLongSet>{

    private final long[] set;

    public KmerLongSet(long[] set) {
        this.set = set;
    }

    @Override
    public int size() {
        return set.length;
    }


    @Override
    public int intersectSize(KmerLongSet other) {
        long[] a = this.set;
        long[] b = other.set;
        int i = 0;
        int j = 0;
        int matches = 0;
        while (i < a.length && j < b.length) {
            if (a[i] != b[j]) {
                if (a[i] < b[j]) {
                    i++;
                } else {
                    j++;
                }
            } else {
                matches++; i++; j++;
            }
        }

        return matches;
    }

    @Override
    public KmerLongSet minHash(int n) {
        if (n <= 0) throw new IllegalArgumentException("N must be positive;");
        n = Math.min(n, size());
        return new KmerLongSet(Arrays.copyOfRange(set, 0, n));
    }

    @Override
    public KmerLongSet mergeMinHash(KmerLongSet other, int n) {
        if (n <= 0) throw new IllegalArgumentException("N must be positive;");
        long[] a = this.set;
        long[] b = other.set;
        long[] out = new long[n];

        int i = 0;
        int j = 0;
        int idx = 0;

        while (i < a.length && j < b.length && idx < n) {
            if (a[i] < b[j]) {
                out[idx++] = a[i];
                i++;
            } else if (a[i] > b[j]) {
                out[idx++] = b[j];
                j++;
            } else {
                out[idx++] = a[i];
                i++;
                j++;
            }
        }
        if (idx < n) {
            while (i < a.length && idx < n) out[idx++] = a[i++];
            while (j < b.length && idx < n) out[idx++] = b[j++];
        }

        return new KmerLongSet(idx < n ? Arrays.copyOf(out, idx) : out);
    }

    public long[] getSet() {
        return set;
    }
}
