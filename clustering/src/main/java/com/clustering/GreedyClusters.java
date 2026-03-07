package com.clustering;


import com.bucket.SmartBuckets;
import com.model.*;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    public void computeClusters(Iterator<? extends Element<V>> elements) {
        Element<V> elem;
        int count = 0;
        int candidate_sum = 0;
        log.info("Starting clustering");
        stats.info("{},{},{},{},{},{},{}", "reads", "avg_candidates", "total_clusters", "total_buckets", "avg_bucket_size", "max_bucket_size", "worstKmer");

        while (elements.hasNext()) {
            elem = elements.next();
            V value = elem.getValue();
            K key = encoder.encode(value);

            Set<C> candidates = universe.getClusterCandidates(key);

            // Logging
            candidate_sum += candidates.size();

            C bestCluster = null;
            String id = elem.getId();

            double minDist = Double.POSITIVE_INFINITY;
            for (C cluster : candidates) {
                if (cluster.isEmpty()) continue;
                double currentDist = universe.distanceToCluster(value, cluster);
                if (currentDist < minDist) {
                    minDist = currentDist;
                    bestCluster = cluster;
                }
            }
            if (bestCluster == null || minDist >= threshold) {
                universe.createCluster(key).addElement(id, value);
            } else {
                bestCluster.addElement(id, value);
            }
            count++;
            if (count % 10_000 == 0) {
                log.info("Reads processed: {}", count);
                SmartBuckets.BucketStats bucketStats = universe.getBucketStats();
                int clusterNum = universe.getClusterCount();
                stats.info("{},{},{},{},{},{},{}", count, candidate_sum/10_000, clusterNum, bucketStats.totalBuckets(), Math.floor(bucketStats.avgBucketSize()), bucketStats.maxBucketSize(), bucketStats.worstKmer());
                candidate_sum = 0;
            }
        }
    }
}
