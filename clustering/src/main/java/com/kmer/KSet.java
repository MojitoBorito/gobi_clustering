package com.kmer;

import java.util.Set;

public class KSet<E>{
    private final Set<E> kmers;

    public KSet(Set<E> kmers) {
        this.kmers = kmers;
    }

    public Set<E> getKmers() {
        return kmers;
    }

    public static <E> int intersectSize(KSet<E> a, KSet<E> b) {
        if (a.size() > b.size()) { // iterate smaller
            KSet<E> tmp = a; a = b; b = tmp;
        }
        int count = 0;
        for (E x : a.kmers) {
            if (b.kmers.contains(x)) count++;
        }
        return count;
    }

    public int size() {
        return kmers.size();
    }
}
