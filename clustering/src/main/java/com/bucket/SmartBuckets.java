package com.bucket;

import com.model.Cluster;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Set;

public interface SmartBuckets<K, C extends Cluster<?>> {
    Set<C> getClusters(K key);
    void add(K key, C cluster);
    void removeCluster(K key, C cluster);
    Set<C> getAllClusters();
    BucketStats getBucketStats();


    record BucketStats(int totalBuckets, double avgBucketSize, int maxBucketSize, long worstKmer) {};
}
