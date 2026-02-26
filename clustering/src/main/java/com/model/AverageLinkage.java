package com.model;

public class AverageLinkage<C extends Cluster<E>, E> implements ClusterLinkage<C, E> {
    @Override
    public double distanceToCluster(DistanceMetric<E> metric, E elem, C cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double sum = 0;
        for (E item : cluster.getElements()) {
           sum += metric.compute(elem, item);
        }
        return sum / cluster.size();
    }
}
