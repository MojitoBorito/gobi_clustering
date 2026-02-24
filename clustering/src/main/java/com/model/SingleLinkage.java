package com.model;

public class SingleLinkage<T> implements ClusterDistance<T>{
    @Override
    public double distanceToCluster(T elem, Cluster<T> cluster) {
        double minDistance = Double.POSITIVE_INFINITY;
        for (T item : cluster.getItems()) {
            minDistance = Math.min(minDistance, cluster.getDistanceMetric().compute(elem, item));
        }
        return minDistance;
    }
}