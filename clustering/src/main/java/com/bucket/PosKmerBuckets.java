package com.bucket;

import com.model.Cluster;

import java.util.*;

public class PosKmerBuckets<C extends Cluster<?>> implements SmartBuckets<long[], C>{

    private final ArrayList<HashMap<Long, Set<C>>> buckets;

    public PosKmerBuckets(int bucketNum) {
        this.buckets = new ArrayList<>(bucketNum);
        for (int i = 0; i < bucketNum; i++) {
            buckets.add(new HashMap<>());
        }
    }


    @Override
    public Set<C> getClusters(long[] key) {
        Set<C> candidates = new HashSet<>();
        for (int idx = 0; idx < key.length; idx++) {
            Set<C> queried = buckets.get(idx).get(key[idx]);
            if (queried == null) continue;
            candidates.addAll(queried);
        }
        return candidates;
    }

    @Override
    public void add(long[] key, C cluster) {
        for (int idx = 0; idx < key.length; idx++) {
            Set<C> clusterSet = buckets.get(idx).computeIfAbsent(key[idx], _ -> new HashSet<>());
            clusterSet.add(cluster);
        }
    }

    @Override
    public void removeCluster(long[] key, C cluster) {
        throw new UnsupportedOperationException("Bleep bloop");
    }

    @Override
    public Set<C> getAllClusters() {
        Set<C> res = new HashSet<>();
        for (HashMap<Long, Set<C>> map : buckets) {
            for (Set<C> clusterSet : map.values()) {
                res.addAll(clusterSet);
            }
        }

        return res;
    }

    @Override
    public BucketStats<Long> getBucketStats() {
        int max = 0;
        int sum = 0;
        long worstKmer = -1;
        for (HashMap<Long, Set<C>> bucket : buckets) {
            for (Map.Entry<Long, Set<C>> entry : bucket.entrySet()) {
                int bucketSize = entry.getValue().size();
                long bucketKmer = entry.getKey();
                sum += bucketSize;
                if (max < bucketSize) {
                    max = bucketSize;
                    worstKmer = bucketKmer;
                }
            }
        }
        int totalEntries = buckets.stream().mapToInt(HashMap::size).sum();
        double avg = totalEntries == 0 ? 0 : (double) sum / totalEntries;

        return new BucketStats<>(totalEntries, avg, max, worstKmer);
    }
}
