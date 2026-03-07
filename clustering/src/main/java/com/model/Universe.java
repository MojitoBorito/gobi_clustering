package com.model;

import com.bucket.SmartBuckets;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;

import java.util.Set;

// Defined by key type K, value type V, Cluster type C
public class Universe<K, V, C extends Cluster<V>>{
    private final SmartBuckets<K, C> buckets;
    private final DistanceMetric<V> metric;
    private final ClusterLinkage<V, C> linkage;
    private final ClusterFactory<C> factory;
    private int clusterCount = 0;

    public Universe(SmartBuckets<K, C> buckets,
                    ClusterFactory<C> factory,
                    DistanceMetric<V> metric,
                    ClusterLinkage<V, C> linkage) {
        this.buckets = buckets;
        this.factory = factory;
        this.metric = metric;
        this.linkage = linkage;
    }

    public C createCluster(K key) {
        C newCluster = factory.create(clusterCount++);
        buckets.add(key, newCluster);
        return newCluster;
    }

    public double distanceToCluster(V value, C cluster) {
        return linkage.distanceToCluster(metric, value, cluster);
    }

    public double distanceBetweenClusters(C a, C b) {
        return linkage.distanceBetweenClusters(a, b);
    }

    public Set<C> getClusterCandidates(K key) {
        return buckets.getClusters(key);
    }

    public Set<C> getAllClusters() {
        return buckets.getAllClusters();
    }

    public SmartBuckets.BucketStats getBucketStats() {
        return buckets.getBucketStats();
    }

    public int getClusterCount() {
        return clusterCount;
    }

    @FunctionalInterface
    public interface ClusterFactory<C> {
        C create(int id);
    }

}
