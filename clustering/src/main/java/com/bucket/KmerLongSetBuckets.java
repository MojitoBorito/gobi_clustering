package com.bucket;

import com.kmer.KmerLongSet;
import com.model.Cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class KmerLongSetBuckets <C extends Cluster<?>> implements SmartBuckets<KmerLongSet, C>{

    int n;
    HashMap<Long, HashSet<C>> clusters;
    boolean addAll;

    public KmerLongSetBuckets(int n, boolean addAll) {
        this.n = n;
        clusters = new HashMap<>();
        this.addAll = addAll;
    }

    @Override
    public Set<C> getClusters(KmerLongSet key) {
        Set<C> resCluster = new HashSet<>();
        long[] set = key.getSet();
        for (int i = 0; i < n; i++) {
            HashSet<C> clusterSet = clusters.getOrDefault(set[i], null);
            if (clusterSet == null) continue;
            resCluster.addAll(clusterSet);
        }
        return resCluster;
    }

    @Override
    public void add(KmerLongSet key, C cluster) {
        int length = addAll ? key.getSet().length : n;
        long[] set = key.getSet();
        for (int i = 0; i < length; i++) {
            clusters.computeIfAbsent(set[i], _ -> new HashSet<>()).add(cluster);
        }
    }

    @Override
    public void removeCluster(KmerLongSet key, C cluster) {
        int length = addAll ? key.getSet().length : n;
        long[] set = key.getSet();
        for (int i = 0; i < length; i++) {
            HashSet<C> clusterSet = clusters.getOrDefault(set[i], null);
            if (cluster == null) continue;
            clusterSet.remove(cluster);
        }
    }

    @Override
    public Set<C> getAllClusters() {
        Set<C> resCluster = new HashSet<>();
        for (HashSet<C> clusterSet : clusters.values()) {
            resCluster.addAll(clusterSet);
        }
        return resCluster;
    }
}
