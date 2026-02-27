package com.metrics;

import com.kmer.KSet;

public class Jaccard<T> implements DistanceMetric<KSet<T>> {
    @Override
    public double compute(KSet<T> t1, KSet<T> t2) {
        int intersectionSize = KSet.intersectSize(t1, t2);
        return (double) intersectionSize / (t1.size() + t2.size() - intersectionSize);
    }
}
