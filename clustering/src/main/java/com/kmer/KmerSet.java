package com.kmer;

public interface KmerSet<T extends KmerSet<T>> {
    int size();
    int intersectSize(T other);
    T minHash(int n);
    T mergeMinHash(T other, int n);
}

