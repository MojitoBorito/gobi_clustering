package com.seeds;

import com.kmer.KmerSet;
import com.model.ClusterSeed;

public class MinHashSeed<V extends KmerSet<V>> implements ClusterSeed<V> {
    private V set;
    private final int n;

    public MinHashSeed(int n) {
        this.n = n;
    }

    @Override
    public void update(V newEntry) {
        if (set == null) set = newEntry.minHash(n);
    }

    @Override
    public V getValue() {
        return set;
    }
}
