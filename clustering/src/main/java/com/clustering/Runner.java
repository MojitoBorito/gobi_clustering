package com.clustering;

import com.bucket.*;
import com.encoding.PosKmerBitEncoder;
import com.encoding.UmiPosKmerBitEncoder;
import com.example.FastqIterator;
import com.kmer.KmerLongSet;
import com.encoding.KmerLongSetEncoder;
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
import com.seeds.ConsensusSeed;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;

public class Runner {
    static void main() {

        UmiAwareBuckets<SeededCluster<UmiRead>> buckets = new UmiAwareBuckets<>(5);
        SeededCluster.SeedFactory<UmiRead> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<UmiRead>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<UmiRead> metric = new UmiReadHamming(5);
        ClusterLinkage<UmiRead, SeededCluster<UmiRead>> linkage = new SeededLinkage<>();
        Encoder<UmiRead, UmiKey> encoder = new UmiPosKmerBitEncoder(50);
        double threshold = 0.03;

        GreedyClusters<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm =
                new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, threshold);
    }
}
