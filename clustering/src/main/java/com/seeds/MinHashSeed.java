package com.seeds;

import com.kmer.KmerSet;
import com.model.ClusterSeed;

public class MinHashSeed<V extends KmerSet<V>> implements ClusterSeed<V> {
    private V set;
    private final int n;

    public MinHashSeed(V initialKmers, int n) {
        this.set = initialKmers.minHash(n);
        this.n = n;
    }

    @Override
    public void update(V newEntry) {
        set = set.mergeMinHash(newEntry, n);
    }

    @Override
    public V getValue() {
        return set;
    }
}
