package com.model;

import com.bucket.SmartBuckets;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Universe<V, C extends Cluster<V>>{
    private final SmartBuckets<V, C> buckets;
    private final DistanceMetric<V> metric;
    private final ClusterLinkage<V, C> linkage;
    private final ClusterFactory<C> factory;
    private int nextId = 0;

    public Universe(SmartBuckets<V, C> buckets,
                    ClusterFactory<C> factory,
                    DistanceMetric<V> metric,
                    ClusterLinkage<V, C> linkage) {
        this.buckets = buckets;
        this.factory = factory;
        this.metric = metric;
        this.linkage = linkage;
    }

    public C createCluster(V key) {
        C newCluster = factory.create(nextId);
        buckets.add(key, newCluster);
        return newCluster;
    }

    public double distanceToCluster(V value, C cluster) {
        return linkage.distanceToCluster(metric, value, cluster);
    }

    public double distanceBetweenClusters(C a, C b) {
        return linkage.distanceBetweenClusters(a, b);
    }

    public Set<C> getClusterCandidates(V key) {
        return buckets.getClusters(key);
    }

    public Set<C> getAllClusters() {
        return buckets.getAllClusters();
    }

    @FunctionalInterface
    public interface ClusterFactory<C> {
        C create(int id);
    }

}
