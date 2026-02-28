package com.kmer;

import java.util.HashSet;
import java.util.TreeSet;

public class KmerStringEncoder extends KmerEncoder{
    public KmerStringEncoder(int k) {
        super(k);
    }
    @Override
    public KmerHashSet<String> encode(String sequence) {
        int k = k();
        if (sequence == null)
            throw new IllegalArgumentException("Sequence cannot be null");
        if (k <= 0 || k > sequence.length())
            throw new IllegalArgumentException("Invalid k");

        HashSet<String> kmers = new HashSet<>();

        for (int i = 0; i <= sequence.length() - k; i++) {
            String kmer = sequence.substring(i, i + k);
            if (kmer.matches("[ACGT]+")) {
                kmers.add(kmer);
            }
        }
        return new KmerHashSet<>(kmers);
    }
}
