package com.kmer;

import com.example.Sequence;
import com.metrics.Jaccard;

import java.util.Arrays;
import java.util.HashSet;

public class KmerLongSetEncoder extends KmerEncoder<KmerLongSet>{

    private final long[] lookup;

    public KmerLongSetEncoder(int k) {
        super(k);
        lookup = new long[128];
        Arrays.fill(lookup, -1);
        lookup['A'] = 0; lookup['T'] = 1; lookup['C'] = 2; lookup['G'] = 3;
    }


    @Override
    public KmerLongSet encode(String sequence) {
        int k = k();
        long mask = (1L << 2*k) - 1;
        long kmer = 0L;
        int valid = 0;
        HashSet<Long> kmers = new HashSet<>();

        for (int i = 0; i < sequence.length(); i++) {
            long base = lookup[sequence.charAt(i)];
            if (base < 0) {kmer = 0; valid = 0; continue;}
            kmer = ((kmer << 2) | base) & mask;
            if (++valid >= k) {
                kmers.add(kmer);
            }
        }

        long[] out = new long[kmers.size()];
        int idx = 0;
        for (Long v : kmers) {
            out[idx++] = v;
        }
        Arrays.sort(out);
        return new KmerLongSet(out);
    }


    static void main() {

    }
}