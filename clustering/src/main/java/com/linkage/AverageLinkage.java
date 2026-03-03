package com.linkage;

import com.model.Cluster;
import com.metrics.DistanceMetric;
import com.model.MaterializedCluster;


public class AverageLinkage<V, C extends MaterializedCluster<V>> implements ClusterLinkage<V, C> {
    @Override
    public double distanceToCluster(DistanceMetric<V> metric, V value, C cluster) {
        if (cluster.isEmpty()) throw new RuntimeException("Can't calculate distance to empty cluster");
        double sum = 0;
        for (V clusterValue : cluster.getValues()) {
           sum += metric.compute(value, clusterValue);
        }
        return sum / cluster.size();
    }
}
