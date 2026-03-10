package com.bucket;

import com.kmer.KmerLongSet;
import com.model.Cluster;

import java.util.*;
import java.util.stream.Collectors;

public class KmerLongSetBuckets <C extends Cluster<?>> implements SmartBuckets<KmerLongSet, C>{

    int n;
    HashMap<Long, HashSet<C>> buckets;
    int clusterCount = 0;
    private final HashMap<C, Integer> scoreBuffer = new HashMap<>();

    public KmerLongSetBuckets(int n) {
        this.n = n;
        buckets = new HashMap<>();
    }

    @Override
    public Set<C> getClusters(KmerLongSet key) {
        Set<C> resCluster = new HashSet<>();
        long[] set = key.getSet();
        for (int i = 0; i < n; i++) {
            HashSet<C> clusterSet = buckets.getOrDefault(set[i], null);
            if (clusterSet == null) continue;
            resCluster.addAll(clusterSet);
        }
        return resCluster;
//        scoreBuffer.clear();
//        long[] set = key.getSet();
//
//        for (int i = 0; i < Math.min(n, set.length); i++) {
//            HashSet<C> clusterSet = buckets.getOrDefault(set[i], null);
//            if (clusterSet == null) continue;
//            for (C cluster : clusterSet) {
//                scoreBuffer.merge(cluster, 1, Integer::sum); // count shared kmers per cluster
//            }
//        }
//
//        // sort by shared kmer count descending
//        return scoreBuffer.entrySet().stream()
//                .sorted((a, b) -> b.getValue() - a.getValue())
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
    }

    @Override
    public void add(KmerLongSet key, C cluster) {
        clusterCount++;
        long[] set = key.getSet();
        for (int i = 0; i < Math.min(set.length, n); i++) {
            Set<C> bucket = buckets.computeIfAbsent(set[i], _ -> new HashSet<>());
            bucket.add(cluster);
        }
    }

    @Override
    public void removeCluster(KmerLongSet key, C cluster) {
        long[] set = key.getSet();
        for (int i = 0; i < Math.min(set.length, n); i++) {
            HashSet<C> clusterSet = buckets.getOrDefault(set[i], null);
            if (cluster == null) continue;
            clusterSet.remove(cluster);
        }
    }

    @Override
    public Set<C> getAllClusters() {
        Set<C> resCluster = new HashSet<>();
        for (HashSet<C> clusterSet : buckets.values()) {
            resCluster.addAll(clusterSet);
        }
        return resCluster;
    }


    @Override
    public BucketStats getBucketStats() {
        int max = 0;
        int sum = 0;
        long worstKmer = Long.MAX_VALUE;
        for (Map.Entry<Long, HashSet<C>> entry : buckets.entrySet()) {
            int bucketSize = entry.getValue().size();
            long bucketKmer = entry.getKey();
            sum += bucketSize;
            if (max < bucketSize) {
                max = bucketSize;
                worstKmer = bucketKmer;
            }
        }
        double avg = buckets.isEmpty() ? 0 : (double) sum / buckets.size();
        return new BucketStats(buckets.size(), avg, max, worstKmer);
    }



}
