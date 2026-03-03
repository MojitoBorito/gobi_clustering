package com.bucket;

import com.kmer.KmerLongSet;
import com.model.Cluster;
import com.model.Element;

import java.util.Set;

public interface SmartBuckets<K, C extends Cluster<K>> {
    Set<C> getClusters(K key);
    void add(K key, C cluster);
    void removeCluster(K key, C cluster);
    Set<C> getAllClusters();
}
