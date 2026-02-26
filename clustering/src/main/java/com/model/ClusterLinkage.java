package com.model;


public interface ClusterLinkage<C extends Cluster<E>, E> {
    double distanceToCluster(DistanceMetric<E> metric, E elem, C cluster);
    // Maybe implement later
    default double distanceBetweenClusters(C a, C b) {
        throw new UnsupportedOperationException();
    }
}