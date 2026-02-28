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
        if (n > size()) throw new IllegalArgumentException("N cannot be greater than set size");
        return new KmerLongSet(Arrays.copyOfRange(set, 0, n));
    }

    @Override
    public KmerLongSet mergeMinHash(KmerLongSet other, int n) {
        if (n > size() + other.size()) throw new IllegalArgumentException("N cannot be greater than the combined set sizes");
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


    static void main() {
        KmerLongSet set1 = new KmerLongSet(new long[]{1, 3, 5, 7, 10});
        KmerLongSet set2 = new KmerLongSet(new long[]{0, 1, 3, 5, 9});
        KmerSet out = set1.mergeMinHash(set2, 3);
        System.out.println();
    }

}
