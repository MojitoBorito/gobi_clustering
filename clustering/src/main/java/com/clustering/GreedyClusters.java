package com.clustering;

import com.bucket.IntersectBuckets;
import com.bucket.KmerLongSetBuckets;
import com.bucket.SmartBuckets;
import com.example.FastqIterator;
import com.example.Sequence;
import com.kmer.KmerLongSet;
import com.kmer.KmerLongSetEncoder;
import com.linkage.AverageLinkage;
import com.linkage.SeededLinkage;
import com.metrics.Jaccard;
import com.model.*;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import com.pipeline.ValueMappingIterator;
import com.seeds.MinHashSeed;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

public class GreedyClusters<V, C extends Cluster<V>> extends ClusteringAlgorithm<V, C>{
    private final double threshold;

    public GreedyClusters(SmartBuckets<V, C> buckets,
                          Universe.ClusterFactory<C> factory,
                          DistanceMetric<V> metric,
                          ClusterLinkage<V, C> linkage,
                          double threshold) {
        super(buckets, factory, metric, linkage);
        this.threshold = threshold;
    }

    public void computeClusters(Iterator<? extends Element<V>> elements) {
        Element<V> elem;
        int count = 0;

        while (elements.hasNext()) {
            elem = elements.next();
            double minDist = Double.POSITIVE_INFINITY;
            Set<C> candidates = universe.getClusterCandidates(elem.getValue());
            C bestCluster = null;
            String id = elem.getId();
            V value = elem.getValue();


            for (C cluster : candidates) {
                if (cluster.isEmpty()) continue;
                double currentDist = universe.distanceToCluster(value, cluster);
                if (currentDist < minDist) {
                    minDist = currentDist;
                    bestCluster = cluster;
                }
            }
            if (bestCluster == null || minDist >= threshold) {
                universe.createCluster(value).addElement(id, value);
            } else {
                bestCluster.addElement(id, value);
            }
            count++;
            if (count % 1000 == 0)
                System.out.println(count);
        }
    }

    static void main() {
        KmerLongSetBuckets<SeededCluster<KmerLongSet>> buckets = new KmerLongSetBuckets<>(5, false);
        //IntersectBuckets<SeededCluster<KmerLongSet>> buckets = new IntersectBuckets<>(5, false);
        FastqIterator reads = new FastqIterator("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/simulation/mock_generation/gen_output/rw.fastq.gz");
        KmerLongSetEncoder enc = new KmerLongSetEncoder(17);
        Iterator<Element<KmerLongSet>> kmers = new ValueMappingIterator<>(reads, enc::encode);
        SeededCluster.ClusterSeedFactory<KmerLongSet> seedFactory = () -> new MinHashSeed<>(200);
        Universe.ClusterFactory<SeededCluster<KmerLongSet>> clusterFactory = (id) -> new SeededCluster<>(id, seedFactory);
        GreedyClusters<KmerLongSet, SeededCluster<KmerLongSet>> clusters =
                new GreedyClusters<>(buckets, clusterFactory, new Jaccard<>(), new SeededLinkage<>(), 1);
        clusters.computeClusters(kmers);
        clusters.writeClustersCompact(Path.of("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/simulation/mock_generation/predicted_clusters/rw.tsv"));
    }
}
