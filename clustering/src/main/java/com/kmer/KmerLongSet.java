package com.kmer;

import java.util.Arrays;
import java.util.Comparator;

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
        if (n < 0 || n > set.length) {
            throw new IllegalArgumentException("KmerLongSet: n must be between 0 and set size");
        }

        Long[] copy = new Long[set.length];
        for (int i = 0; i < set.length; i++) {
            copy[i] = set[i];
        }

        Arrays.sort(copy, Comparator.comparingLong(KmerLongSet::mix64));

        long[] result = new long[n];
        for (int i = 0; i < n; i++) {
            result[i] = copy[i];
        }

        return new KmerLongSet(result);
    }

    public static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
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
