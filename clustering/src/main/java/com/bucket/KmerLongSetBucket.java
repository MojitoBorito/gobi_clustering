package com.bucket;

import com.kmer.KmerLongSet;
import com.model.Cluster;
import com.model.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class KmerLongSetBucket <C extends Cluster<KmerLongSet>> implements SmartBuckets<Element<KmerLongSet>, C>{

    int n;
    HashMap<Long, HashSet<C>> clusters;

    public KmerLongSetBucket(int n) {
        this.n = n;
        clusters = new HashMap<>();
    }

    @Override
    public Set<C> getClusters(Element<KmerLongSet> key) {
        Set<C> resCluster = new HashSet<>();
        long[] set = key.getValue().getSet();
        for (int i = 0; i < n; i++) {
            HashSet<C> clusterSet = clusters.getOrDefault(set[i], null);
            if (clusterSet == null) continue;
            resCluster.addAll(clusterSet);
        }
        return resCluster;
    }

    @Override
    public void add(Element<KmerLongSet> key, C cluster) {
        long[] set = key.getValue().getSet();
        for (int i = 0; i < n; i++) {
            clusters.computeIfAbsent(set[i], _ -> new HashSet<>()).add(cluster);
        }
    }

    @Override
    public void removeCluster(Element<KmerLongSet> key, C cluster) {
        long[] set = key.getValue().getSet();
        for (int i = 0; i < n; i++) {
            HashSet<C> clusterSet = clusters.getOrDefault(set[i], null);
            if (cluster == null) continue;
            clusterSet.remove(cluster);
        }
    }
}
