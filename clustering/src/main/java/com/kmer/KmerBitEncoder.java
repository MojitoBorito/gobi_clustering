package com.kmer;

import java.util.*;

public class KmerBitEncoder extends KmerEncoder<Long>{
    // MASKS
    private static long A = 0;
    private static long T = 1;
    private static long C = 2;
    private static long G = 3;

    public KmerBitEncoder(int k) {
        if (k > 32) {
            throw new IllegalArgumentException("Max kmer size: 32");
        }
        super(k);
    }

    @Override
    public KSet<Long> encode(String sequence) {
        Set<Long> kmers = new HashSet<>();
        int k = k();
        for (int i = 0; i < sequence.length() - k + 1; i++) {
            long kmer = 0L;
            boolean valid = true;
            w: for (int j = 0; j < k; j++) {
                kmer <<= 2;
                switch (sequence.charAt(i+j)) {
                    case 'A': kmer |= A; break;
                    case 'T': kmer |= T; break;
                    case 'C': kmer |= C; break;
                    case 'G': kmer |= G; break;
                    default: i = i+j; valid = false; break w; // Invalid char, reset window
                }
            }
            if (valid) kmers.add(kmer);
        }
        return new KSet<>(kmers);
    }

    public Set<String> decode(KSet<Long> kmers) {
        int k = k();
        Set<String> decoded = new HashSet<>();
        for (Long kmer : kmers.getKmers()) {
            decoded.add(decodeKmer(kmer));
        }
        return decoded;
    }

    public String decodeKmer(Long kmer) {
        int k = k();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < k; i++) {
            int twoBits = (int) (kmer >> 2*(k-1-i)) & 0b11;
            char base = switch (twoBits) {
                case 0 -> 'A';
                case 1 -> 'T';
                case 2 -> 'C';
                case 3 -> 'G';
                default -> throw new IllegalStateException("Unexpected value: " + twoBits);
            };
            sb.append(base);
        }
        return sb.toString();
    }
}
