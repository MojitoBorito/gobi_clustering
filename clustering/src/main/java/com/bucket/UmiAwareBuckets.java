package com.bucket;

import com.model.Cluster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UmiAwareBuckets<C extends Cluster<?>> implements SmartBuckets<UmiKey, C>{

    private final HashMap<String, PosKmerBuckets<C>> umiToBuckets = new HashMap<>();
    private final int proUmiBucketNum;

    public UmiAwareBuckets(int proUmiBucketNum) {
        this.proUmiBucketNum = proUmiBucketNum;
    }

    @Override
    public Set<C> getClusters(UmiKey key) {
        PosKmerBuckets<C> buckets = umiToBuckets.getOrDefault(key.umi(), null);
        if (buckets == null) return null;
        return buckets.getClusters(key.kmers());
    }

    @Override
    public void add(UmiKey key, C cluster) {
        PosKmerBuckets<C> buckets = umiToBuckets.computeIfAbsent(key.umi(), _ -> new PosKmerBuckets<>(proUmiBucketNum));
        buckets.add(key.kmers(), cluster);
    }

    @Override
    public void removeCluster(UmiKey key, C cluster) {
        throw new UnsupportedOperationException("lol");
    }

    @Override
    public Set<C> getAllClusters() {
        Set<C> clusters = new HashSet<>();
        for (PosKmerBuckets<C> buckets : umiToBuckets.values()) {
            clusters.addAll(buckets.getAllClusters());
        }
        return clusters;
    }

    @Override
    public BucketStats<?> getBucketStats() {
        int maxBucketSize = 0;
        int totalBuckets = 0;
        double avgBucketSize = 0;
        long worstKey = -1;
        for (PosKmerBuckets<C> buckets : umiToBuckets.values()) {
            BucketStats<Long> stats = buckets.getBucketStats();
            totalBuckets += stats.totalBuckets();
            avgBucketSize += stats.avgBucketSize()/umiToBuckets.size();
            if (stats.maxBucketSize() > maxBucketSize) {
                maxBucketSize = stats.maxBucketSize();
                worstKey = stats.worstKey();
            }
        }

        return new BucketStats<>(totalBuckets, avgBucketSize, maxBucketSize, worstKey);
    }
}

