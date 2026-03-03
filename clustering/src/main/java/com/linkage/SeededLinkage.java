package com.linkage;

import com.metrics.DistanceMetric;
import com.model.MaterializedCluster;
import com.model.SeededCluster;

public class SeededLinkage<V, C extends SeededCluster<V>> implements ClusterLinkage<V, C>{

    @Override
    public double distanceToCluster(DistanceMetric<V> metric, V elem, C cluster) {
        return metric.compute(elem, cluster.getSeed());
    }
}
