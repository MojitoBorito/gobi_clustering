package com.linkage;

import com.model.Cluster;
import com.metrics.DistanceMetric;

public class SingleLinkage<C extends Cluster<E>, E> implements ClusterLinkage<C, E> {
    @Override
    public double distanceToCluster(DistanceMetric<E> metric, E elem, C cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double minDistance = Double.POSITIVE_INFINITY;
        for (E item : cluster.getElements()) {
            minDistance = Math.min(minDistance, metric.compute(elem, item));
        }
        return minDistance;
    }
}