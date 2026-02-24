package com.model;

public class AverageLinkage<T> implements ClusterDistance<T>{
    @Override
    public double distanceToCluster(T elem, Cluster<T> cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double sum = 0;
        for (T item : cluster.getItems()) {
           sum += cluster.getDistanceMetric().compute(elem, item);
        }
        return sum / cluster.size();
    }
}
