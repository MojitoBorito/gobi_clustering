package com.linkage;

import com.model.Cluster;
import com.metrics.DistanceMetric;
import com.model.MaterializedCluster;

public class SingleLinkage<C extends MaterializedCluster<V>, V> implements ClusterLinkage<C, V> {
    @Override
    public double distanceToCluster(DistanceMetric<V> metric, V value, C cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double minDistance = Double.POSITIVE_INFINITY;
        for (V clusterValue : cluster.getValues()) {
            minDistance = Math.min(minDistance, metric.compute(value, clusterValue));
        }
        return minDistance;
    }
}