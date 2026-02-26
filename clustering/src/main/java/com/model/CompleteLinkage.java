package com.model;

public class CompleteLinkage<C extends Cluster<E>, E> implements ClusterLinkage<C, E> {

    @Override
    public double distanceToCluster(DistanceMetric<E> metric, E elem, C cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double maxDistance = Double.POSITIVE_INFINITY;
        for (E item : cluster.getElements()) {
            maxDistance = Math.min(maxDistance, metric.compute(elem, item));
        }
        return maxDistance;
    }
}
