package com.bucket;

import com.kmer.KmerLongSet;
import com.model.Cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class KmerLongSetBuckets <C extends Cluster<KmerLongSet>> implements SmartBuckets<KmerLongSet, C>{

    int n;
    HashMap<Long, HashSet<C>> clusters;

    public KmerLongSetBuckets(int n) {
        this.n = n;
        clusters = new HashMap<>();
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
        long[] set = key.getSet();
        for (int i = 0; i < n; i++) {
            clusters.computeIfAbsent(set[i], _ -> new HashSet<>()).add(cluster);
        }
    }

    @Override
    public void removeCluster(KmerLongSet key, C cluster) {
        long[] set = key.getSet();
        for (int i = 0; i < n; i++) {
            HashSet<C> clusterSet = clusters.getOrDefault(set[i], null);
            if (cluster == null) continue;
            clusterSet.remove(cluster);
        }
    }
}
