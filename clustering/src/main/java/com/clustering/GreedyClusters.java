package com.clustering;


import com.bucket.SmartBuckets;
import com.model.*;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import java.util.Iterator;
import java.util.Set;

// Is defined by key type K, value type V, cluster type V
public class GreedyClusters<K, V, C extends Cluster<V>> extends ClusteringAlgorithm<K, V, C>{
    // Maximal distance threshold. If exceeded a new cluster is created
    private final double threshold;
    // Value to key encoder. Key is used for bucket lookup
    private final Encoder<V, K> encoder;

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

        while (elements.hasNext()) {
            elem = elements.next();
            V value = elem.getValue();
            K key = encoder.encode(value);

            Set<C> candidates = universe.getClusterCandidates(key);
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
            if (count % 1000 == 0)
                System.out.println(count);
        }
    }
}
