package com.model;


public interface ClusterLinkage<K, C extends Cluster<K, E>, E> {
    double distanceToCluster(DistanceMetric<E> metric, E elem, C cluster);
    // Maybe implement later
    default double distanceBetweenClusters(C a, C b) {
        throw new UnsupportedOperationException();
    }
}