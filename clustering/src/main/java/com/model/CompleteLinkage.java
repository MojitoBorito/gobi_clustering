package com.model;

public class CompleteLinkage<T> implements ClusterDistance<T>{

    @Override
    public double distanceToCluster(T elem, Cluster<T> cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double maxDistance = Double.POSITIVE_INFINITY;
        for (T item : cluster.getItems()) {
            maxDistance = Math.min(maxDistance, cluster.getDistanceMetric().compute(elem, item));
        }
        return maxDistance;
    }
}
