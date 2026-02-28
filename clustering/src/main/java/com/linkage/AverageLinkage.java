package com.linkage;

import com.model.Cluster;
import com.metrics.DistanceMetric;
import com.model.MaterializedCluster;


public class AverageLinkage<C extends MaterializedCluster<V>, V> implements ClusterLinkage<C, V> {
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
