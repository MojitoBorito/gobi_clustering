package com.linkage;


import com.model.Cluster;
import com.metrics.DistanceMetric;

public interface ClusterLinkage<V, C extends Cluster<V>> {
    double distanceToCluster(DistanceMetric<V> metric, V value, C cluster);
    // Maybe implement later
    default double distanceBetweenClusters(C a, C b) {
        throw new UnsupportedOperationException();
    }
}