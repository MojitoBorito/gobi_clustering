package com.kmer;

import java.util.*;

public class KmerHashSet<E> implements KmerSet<KmerHashSet<E>>{

    private final HashSet<E> set;

    public KmerHashSet(HashSet<E> set) {
        this.set = set;
    }

    @Override
    public KmerHashSet<E> minHash(int n) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KmerHashSet<E> mergeMinHash(KmerHashSet<E> other, int n) {
        throw new UnsupportedOperationException("Not implemented yet");
    }


    @Override
    public int size() {
        return set.size();
    }

    @Override
    public int intersectSize(KmerHashSet<E> other) {
        Set<E> a = this.set;
        Set<E> b = other.set;
        if (a.size() > b.size()) { // iterate smaller
            Set<E> tmp = a; a = b; b = tmp;
        }
        int count = 0;
        for (E x : a) {
            if (b.contains(x)) count++;
        }
        return count;
    }

    public HashSet<E> getSet() {
        return set;
    }

    static void main() {

    }
}
