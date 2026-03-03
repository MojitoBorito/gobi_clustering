package com.metrics;

import com.kmer.KmerSet;

public class Jaccard<T extends KmerSet<T>> implements DistanceMetric<T> {

    @Override
    public double compute(T e1, T e2) {
        int intersectionSize = e1.intersectSize(e2);
        return 1 - ((double) intersectionSize / (e1.size() + e2.size() - intersectionSize));
    }
}
