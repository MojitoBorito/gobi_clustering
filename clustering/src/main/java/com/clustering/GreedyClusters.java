package com.clustering;

import com.bucket.BucketStats;
import com.bucket.SmartBuckets;
import com.model.*;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.Set;


// Is defined by key type K, value type V, cluster type V
public class GreedyClusters<K, V, C extends Cluster<V>> extends ClusteringAlgorithm<K, V, C>{
    // Maximal distance threshold. If exceeded a new cluster is created
    private final double threshold;
    // Value to key encoder. Key is used for bucket lookup
    private final Encoder<V, K> encoder;
    private static final Logger log = LoggerFactory.getLogger(GreedyClusters.class);
    private static final Logger stats = LoggerFactory.getLogger("stats");

    public GreedyClusters(SmartBuckets<K, C> buckets,
                          Universe.ClusterFactory<C> factory,
                          DistanceMetric<V> metric,
                          ClusterLinkage<V, C> linkage,
                          Encoder<V, K> valueToKeyEncoder,
                          double threshold) {
        super(buckets, factory, metric, linkage);
        this.threshold = threshold;
        this.encoder = valueToKeyEncoder;
    }

    public void computeClustersLogged(Iterator<? extends Element<V>> elements, java.io.BufferedWriter output) {
        Element<V> elem;
        int count = 0;
        int candidate_sum = 0;
        log.info("Starting clustering");
        stats.info("{},{},{},{},{},{},{}", "reads", "avg_candidates","total_clusters", "total_buckets", "avg_bucket_size", "max_bucket_size", "worstKmer");

        while (elements.hasNext()) {
            elem = elements.next();
            V value = elem.getValue();
            K key = encoder.encode(value);

            Set<C> candidates = universe.getClusterCandidates(key);
            candidate_sum += candidates == null ? 0 : candidates.size();

            C bestCluster = extractBestClusterCandidate(value, candidates, threshold);
            String id = elem.getId();

            if (bestCluster == null) {
                bestCluster = universe.createCluster(key);
            }
            bestCluster.addElement(id, value);

            // write to output
            try {
                output.write(bestCluster.getId() + "\t" + id + "\t" + value);
                output.newLine();
            } catch (java.io.IOException e) {
                log.error("Failed to write output for read {}", id, e);
            }

            count++;
            if (count % 10_000 == 0) {
                log.info("Reads processed: {}", count);
                try {
                    output.flush(); // flush periodically so file isn't empty if killed
                } catch (java.io.IOException e) {
                    log.error("Failed to flush output", e);
                }
                BucketStats<?> bucketStats = universe.getBucketStats();
                int clusterNum = universe.getClusterCount();
                stats.info("{},{},{},{},{},{},{}", count, candidate_sum/10_000, clusterNum, bucketStats.totalBuckets(), Math.floor(bucketStats.avgBucketSize()), bucketStats.maxBucketSize(), bucketStats.worstKey());
                candidate_sum = 0;
            }
        }
    }

    @Override
    public void computeClusters(Iterator<? extends Element<V>> elements) {
        Element<V> elem;

        while (elements.hasNext()) {
            elem = elements.next();
            V value = elem.getValue();
            K key = encoder.encode(value);

            Set<C> candidates = universe.getClusterCandidates(key);
            C bestCluster = extractBestClusterCandidate(value, candidates, threshold);
            String id = elem.getId();

            if (bestCluster == null) {
                bestCluster = universe.createCluster(key);
            }
            bestCluster.addElement(id, value);
        }
    }

    public C extractBestClusterCandidate(V value, Set<C> candidates, double threshold) {
        C bestCluster = null;

        double minDist = Double.POSITIVE_INFINITY;

        if (candidates == null) return null;

        for (C cluster : candidates) {
            if (cluster.isEmpty()) continue;
            double currentDist = universe.distanceToCluster(value, cluster);
            if (currentDist < minDist) {
                minDist = currentDist;
                bestCluster = cluster;
            }
        }
        if (minDist >= threshold) {
            return null;
        }
        return bestCluster;
    }
}