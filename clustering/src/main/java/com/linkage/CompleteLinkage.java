package com.linkage;

import com.metrics.DistanceMetric;
import com.model.MaterializedCluster;

public class CompleteLinkage<V, C extends MaterializedCluster<V>> implements ClusterLinkage<V, C> {

    @Override
    public double distanceToCluster(DistanceMetric<V> metric, V value, C cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double maxDistance = Double.POSITIVE_INFINITY;
        for (V clusterValue: cluster.getValues()) {
            maxDistance = Math.min(maxDistance, metric.compute(value, clusterValue));
        }
        return maxDistance;
    }
}
