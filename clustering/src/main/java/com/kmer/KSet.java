package com.kmer;

import java.util.Set;

public class KSet<E> {
    private final Set<E> kmers;

    public KSet(Set<E> kmers) {
        this.kmers = kmers;
    }

    public Set<E> getKmers() {
        return kmers;
    }
}
