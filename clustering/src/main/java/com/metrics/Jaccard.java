package com.metrics;

import com.kmer.KmerSet;

public class Jaccard<T> implements DistanceMetric<KmerSet> {
    @Override
    public double compute(KmerSet t1, KmerSet t2) {
        int intersectionSize = t1.intersectSize(t2);
        return 1 - ((double) intersectionSize / (t1.size() + t2.size() - intersectionSize));
    }
}
