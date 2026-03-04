package com.bucket;

import com.kmer.KmerLongSet;
import com.model.Cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class IntersectBuckets<C extends Cluster<KmerLongSet>> implements SmartBuckets<KmerLongSet, C>{

    int n;
    HashMap<Long, HashSet<C>> unionBuckets;
    HashMap<Long, HashSet<C>> intersectBuckets;
    boolean addAll;

    public IntersectBuckets(int n, boolean addAll){
        this.n = n;
        this.unionBuckets = new HashMap<>();
        this.intersectBuckets = new HashMap<>();
        this.addAll = addAll;
    }

    @Override
    public Set<C> getClusters(KmerLongSet key) {
        long hash = computeFingerprint(key);
        Set<C> res = intersectBuckets.getOrDefault(hash, null);
        if (res != null) return res;

        res = new HashSet<>();
        long[] set = key.getSet();
        for (int i = 0; i < n; i++) {
            HashSet<C> clusterSet = unionBuckets.getOrDefault(set[i], null);
            if (clusterSet == null) continue;
            res.addAll(clusterSet);
        }
        return res;
    }

    @Override
    public void add(KmerLongSet key, C cluster) {
        intersectBuckets.computeIfAbsent(computeFingerprint(key), _ -> new HashSet<>()).add(cluster);

        int length = addAll ? key.getSet().length : n;
        long[] set = key.getSet();
        for (int i = 0; i < length; i++) {
            unionBuckets.computeIfAbsent(set[i], _ -> new HashSet<>()).add(cluster);
        }
    }

    @Override
    public void removeCluster(KmerLongSet key, C cluster) {
        HashSet<C> clusterSet = intersectBuckets.getOrDefault(computeFingerprint(key), null);
        if (clusterSet != null) clusterSet.remove(cluster);

        int length = addAll ? key.getSet().length : n;
        long[] set = key.getSet();
        for (int i = 0; i < length; i++) {
            clusterSet = unionBuckets.getOrDefault(set[i], null);
            if (cluster == null) continue;
            clusterSet.remove(cluster);
        }
    }

    @Override
    public Set<C> getAllClusters() {
        Set<C> res = new HashSet<>();
        for (HashSet<C> clusterSet : unionBuckets.values()) {
            res.addAll(clusterSet);
        }
        return res;
    }

    private static long murmurMix(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    private long computeFingerprint(KmerLongSet key) {
        long[] set = key.getSet();
        int len = Math.min(n, set.length);

        long hash = len; // seed with the count

        for (int i = 0; i < len; i++) {
            long k = murmurMix(set[i]);
            hash ^= k;
            hash = Long.rotateLeft(hash, 27);
            hash = hash * 5 + 0x52dce729;
        }

        // Final avalanche
        return murmurMix(hash);
    }

}
