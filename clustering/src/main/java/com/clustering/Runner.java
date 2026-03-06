package com.clustering;

import com.bucket.IntersectBuckets;
import com.bucket.KmerLongSetBuckets;
import com.bucket.SmartBuckets;
import com.example.FastqIterator;
import com.kmer.KmerLongSet;
import com.kmer.KmerLongSetEncoder;
import com.linkage.ClusterLinkage;
import com.linkage.SeededLinkage;
import com.metrics.DistanceMetric;
import com.metrics.Hamming;
import com.model.Element;
import com.model.Encoder;
import com.model.SeededCluster;
import com.model.Universe;
import com.seeds.AnchorSeed;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class Runner {
    static void main() {
        SmartBuckets<KmerLongSet, SeededCluster<String>> buckets = new IntersectBuckets<>(5, false);
        SeededCluster.SeedFactory<String> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<String>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<String> metric = new Hamming();
        ClusterLinkage<String, SeededCluster<String>> linkage = new SeededLinkage<>();
        Encoder<String, KmerLongSet> encoder = new KmerLongSetEncoder(17);
        double threshold = 0.03;

        GreedyClusters<KmerLongSet, String, SeededCluster<String>> algorithm =
                new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, threshold);

        String path = "/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/simulation/larger_generation/fw.fastq.gz";
        String out = "/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/simulation/larger_generation/fw.tsv";
        try (FastqIterator sequences = new FastqIterator(path)) {
            algorithm.computeClusters(sequences);
            algorithm.writeClustersCompact(Path.of(out));
        } catch (Exception e) {
            throw new RuntimeException("Failed");
        }
    }
}
