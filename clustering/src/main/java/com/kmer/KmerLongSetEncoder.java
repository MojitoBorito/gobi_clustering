package com.kmer;

import com.metrics.Jaccard;

import java.util.Arrays;
import java.util.HashSet;

public class KmerLongSetEncoder extends KmerEncoder{

    private final long[] lookup;

    public KmerLongSetEncoder(int k) {
        super(k);
        lookup = new long[128];
        Arrays.fill(lookup, -1);
        lookup['A'] = 0; lookup['T'] = 1; lookup['C'] = 2; lookup['G'] = 3;
    }

    /**
    Important: ASCII encoded
     **/
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
        KmerLongSetEncoder enc = new KmerLongSetEncoder(15);
        Jaccard<KmerLongSet> jac = new Jaccard<>();
        int shift = 15;
        String seq1 = SequenceUtils.randomSequence(150, 0);
        String seq2 = SequenceUtils.randomSequence(shift, 0 ) + seq1.substring(0, seq1.length() - shift);
        System.out.println(seq1);
        System.out.println(seq2);
        KmerLongSet seq1e = enc.encode(seq1);
        KmerLongSet seq2e = enc.encode(seq2);
        System.out.println(jac.compute(seq1e, seq2e));
    }
}