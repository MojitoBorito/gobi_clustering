package com.clustering;

import com.bucket.*;
import com.encoding.UmiPosKmerBitEncoder;
import com.linkage.ClusterLinkage;
import com.linkage.SeededLinkage;
import com.metrics.DistanceMetric;
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
        System.out.println("STARTING");
        UmiAwareBuckets<SeededCluster<UmiRead>> buckets = new UmiAwareBuckets<>(5);
        SeededCluster.SeedFactory<UmiRead> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<UmiRead>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<UmiRead> metric = new UmiReadHamming(5);
        ClusterLinkage<UmiRead, SeededCluster<UmiRead>> linkage = new SeededLinkage<>();
        Encoder<UmiRead, UmiKey> encoder = new UmiPosKmerBitEncoder(30);
        double threshold = 0.03;

        GreedyClusters<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm =
                new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, threshold);

        String path = "/mnt/biocluster/praktikum/genprakt/gruppe_g/dual_clustering/H1-12936-T2/R1/clusters.txt";
        String out = "/mnt/biocluster/praktikum/genprakt/mitsopoulos/block/cluster_output";
        try (MohitParser parser = new MohitParser(Path.of(path));
             BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(out, "full_clusters.txt").toString()))) {
            algorithm.computeClustersLogged(parser, writer);
            algorithm.writeClustersCompact(Path.of(out));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
