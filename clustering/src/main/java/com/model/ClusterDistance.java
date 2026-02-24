package com.model;

import java.util.Collection;

public interface ClusterDistance<T> {
    double distanceToCluster(T elem, Cluster<T> cluster);
    // Maybe implement later
    default double distanceBetweenClusters(Cluster<T> a, Cluster<T> b) {
        throw new UnsupportedOperationException();
    }
}