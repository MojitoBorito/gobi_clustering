public package com.model;

import java.util.Collection;

public interface ClusterDistance<T> {
    double distanceToCluster(T elem, Cluster<T> cluster);
    // Maybe implement later
    default double distanceBetweenClusters(Collection<T> a, Collection<T> b) {
        throw new UnsupportedOperationException();
    }
}