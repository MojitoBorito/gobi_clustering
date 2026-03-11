package com.clustering;

import com.bucket.*;
import com.encoding.PosKmerBitEncoder;
import com.encoding.UmiPosKmerBitEncoder;
import com.example.FastqIterator;
import com.linkage.ClusterLinkage;
import com.linkage.SeededLinkage;
import com.metrics.DistanceMetric;
import com.metrics.Hamming;
import com.metrics.UmiReadHamming;
import com.model.Encoder;
import com.model.SeededCluster;
import com.model.UmiRead;
import com.model.Universe;
import com.seeds.AnchorSeed;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;

public class Runner {
    public static void main(String[] args) {
//        System.out.println("STARTING");
//        UmiAwareBuckets<SeededCluster<UmiRead>> buckets = new UmiAwareBuckets<>(5);
//        SeededCluster.SeedFactory<UmiRead> seedFactory = AnchorSeed::new;
//        Universe.ClusterFactory<SeededCluster<UmiRead>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
//        DistanceMetric<UmiRead> metric = new UmiReadHamming(5);
//        ClusterLinkage<UmiRead, SeededCluster<UmiRead>> linkage = new SeededLinkage<>();
//        Encoder<UmiRead, UmiKey> encoder = new UmiPosKmerBitEncoder(30);
//        double threshold = 0.03;


        PosKmerBuckets<SeededCluster<String>> buckets = new PosKmerBuckets<>(5);
        SeededCluster.SeedFactory<String> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<String>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<String> metric = new Hamming(5);
        ClusterLinkage<String, SeededCluster<String>> linkage = new SeededLinkage<>();
        Encoder<String, long[]> encoder = new PosKmerBitEncoder(30);
        double threshold = 0.03;

        GreedyClusters<long[], String, SeededCluster<String>> algorithm =
                new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, threshold);


        try (MohitParser parser = new MohitParser(Path.of("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/clusters.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/out.txt"))) {
            algorithm.computeClustersLogged(parser, writer);
            algorithm.writeClustersCompact(Path.of("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/cluster_out.txt"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
