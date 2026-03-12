package com.example;

import com.bucket.PosKmerBuckets;
import com.bucket.UmiAwareBuckets;
import com.bucket.UmiKey;
import com.cli.CliParser;
import com.cli.CmdOptions;
import com.clustering.ClusteringAlgorithm;
import com.clustering.GreedyClusters;
import com.encoding.PosKmerBitEncoder;
import com.encoding.UmiPosKmerBitEncoder;
import com.filter.*;
import com.linkage.ClusterLinkage;
import com.linkage.SeededLinkage;
import com.metrics.DistanceMetric;
import com.metrics.Hamming;
import com.metrics.UmiReadHamming;
import com.model.*;
import com.seeds.AnchorSeed;
import org.apache.commons.cli.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws ParseException {
        CmdOptions options = CliParser.parse(args);

        Statistics.umiEdits = new int[12];
        Statistics.AnchorEdits = new int[150];

        System.out.println("Starting clustering...");
        System.out.println("Perform umi clustering: " + options.runPrimaryClustering());
        System.out.println("Perform secondary cycle: " + options.runSecondaryClustering());

        // If no umi path is provided, perform clustering without them
        if (options.runSecondaryClustering() && !options.runPrimaryClustering()) {
            long start = System.currentTimeMillis();
            try {
                FastqIterator reads = new FastqIterator(options.reads());
                ClusteringAlgorithm<long[], String, SeededCluster<String>> algorithm =
                        initSimpleClusteringAlgorithm(
                                options.readLength(),
                                options.kmerSize(),
                                options.threshold()
                        );
                algorithm.computeClusters(reads);
                algorithm.writeClustersCompact(Paths.get(options.outDir(), "simple_clusters.txt"));

                long end = System.currentTimeMillis();
                System.out.println("Simple clustering time: " + (end - start) + " ms");
                System.out.println("Simple clustering time: " + ((end - start) / 1000.0) + " s");
                return;
            } catch (Exception e) {
                throw new RuntimeException("Error during simple clustering", e);
            } finally {
                long end = System.currentTimeMillis();
                System.out.println("Total runtime: " + (end - start) / 1000.0 + " s");
                System.out.println("Total runtime: " + ((end - start) / (1000.0*60)) + " min");
            }
        }

        // PRIMARY CLUSTERING
        long starTime = System.currentTimeMillis();

        UMI umiGroup = new UMI(options.umi());

        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        ImprovedDualClustering improvedDualClustering = new ImprovedDualClustering(umiGroup, options.reads());

        endTime = System.currentTimeMillis();
        long second = endTime - starTime;

        System.out.println("umi clustering time: "+first);
        System.out.println("Dual clustering time: "+second+"\n");
        List<CorrectedUMICluster> umiClusters = improvedDualClustering.getClusters();


        writeOutput(options, improvedDualClustering, umiClusters);

        // SECONDARY CLUSTERING
        if (options.runSecondaryClustering()) {
            System.out.println("Starting finer clustering...");
            long start = System.currentTimeMillis();
            ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm = runWithUmis(umiClusters, options.readLength(), options.kmerSize(), options.threshold());
            long finerClusteringEnd = System.currentTimeMillis();
            System.out.println("Finer clustering generation time: " + ((finerClusteringEnd - start) / (1000.0*60)) + " min");
            System.out.println("Writing finer cluster output...");
            long outputStart = System.currentTimeMillis();
            Set<SeededCluster<UmiRead>> computedClusters = algorithm.getAllClusters();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(options.outDir(), "finer_clusters.txt").toString()))) {
                writer.write("cluster_id");
                writer.write('\t');
                writer.write("read_id");
                writer.write('\n');
                HashMap<Integer, Set<String>> subClusterIDToHeader= improvedDualClustering.getClusterIDtoHeader();
                for (SeededCluster<UmiRead> cluster : computedClusters) {
                    for (String subClusterId : cluster.getElementIds()) {
                        for (String readId : subClusterIDToHeader.get(Integer.parseInt(subClusterId))) {
                            writer.write(cluster.getId());
                            writer.write('\t');
                            writer.write(readId);
                            writer.write('\n');
                        }
                    }
                }
                long outputEnd = System.currentTimeMillis();
                System.out.println("Finer cluster write time: " + ((outputEnd-outputStart) / (1000.0)) + " ms");
            } catch (Exception e) {
                throw new RuntimeException("Error writing finer clusters", e);
            }
            long end = System.currentTimeMillis();
            System.out.println("Total finer clustering time: " + ((end - start) / (1000.0*60)) + " min");
        }
    }

    private static ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> runWithUmis(List<CorrectedUMICluster> umiClusters, int readLength, int kmerSize, double threshold) {
        Iterator<Element<UmiRead>> iteratorMap = new Iterator<>() {
            final Iterator<CorrectedUMICluster> inner = umiClusters.iterator();

            @Override
            public boolean hasNext() { return inner.hasNext(); }

            @Override
            public Element<UmiRead> next() {
                CorrectedUMICluster u = inner.next();
                return new Element<>(String.valueOf(u.getClusterID()), new UmiRead(u.getUmi(), u.getRead()));
            }
        };

        ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm = initUmiAwareAlgorithm(readLength, kmerSize, threshold);
        algorithm.computeClusters(iteratorMap);
        return algorithm;
    }


    private static ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> initUmiAwareAlgorithm(int readLength, int kmerSize, double distanceThreshold) {
        checkValid(kmerSize, readLength);
        UmiAwareBuckets<SeededCluster<UmiRead>> buckets = new UmiAwareBuckets<>(readLength/kmerSize);
        SeededCluster.SeedFactory<UmiRead> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<UmiRead>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<UmiRead> metric = new UmiReadHamming((int) (readLength*distanceThreshold) + 1);
        ClusterLinkage<UmiRead, SeededCluster<UmiRead>> linkage = new SeededLinkage<>();
        Encoder<UmiRead, UmiKey> encoder = new UmiPosKmerBitEncoder(kmerSize);
        return new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, distanceThreshold);
    }

    private static ClusteringAlgorithm<long[], String, SeededCluster<String>> initSimpleClusteringAlgorithm(int readLength, int kmerSize, double distanceThreshold) {
        checkValid(kmerSize, readLength);
        PosKmerBuckets<SeededCluster<String>> buckets = new PosKmerBuckets<>(readLength/kmerSize);
        SeededCluster.SeedFactory<String> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<String>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<String> metric = new Hamming((int) (readLength*distanceThreshold) + 1);
        ClusterLinkage<String, SeededCluster<String>> linkage = new SeededLinkage<>();
        Encoder<String, long[]> encoder = new PosKmerBitEncoder(kmerSize);

        return new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, distanceThreshold);
    }

    private static void checkValid(int kmerSize, int readLength) {
        if (kmerSize <= 0) throw new IllegalArgumentException("kmerSize must be > 0");
        if (readLength <= 0) throw new IllegalArgumentException("readLength must be > 0");
        if (kmerSize > readLength) throw new IllegalArgumentException("kmerSize cannot be larger than readLength");
    }



    public static void writeOutput(CmdOptions options, ImprovedDualClustering improvedDualClustering, List<CorrectedUMICluster> clusters) {
        int n = 0;
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.outDir(), "clusters.txt"))) {
            writer.write("ID\tUMI\tseq\tcounts\n");
            for (CorrectedUMICluster cluster : clusters){
                writer.write(cluster.getClusterID()+"\t"+cluster.getUmi()+"\t"+cluster.getRead()+"\t"+cluster.getCount()+"\n");
                n++;

            }
        } catch (IOException e) {
            throw new RuntimeException("Error at writing Cluster Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.outDir(), "umi_counts.txt"))){
            HashMap<String, Integer> correctedUmis = improvedDualClustering.getCorrectedUmis();
            writer.write("corrected_UMI\tcounts\n");
            for (Map.Entry<String, Integer> entry : correctedUmis.entrySet()){
                writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Umi Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.outDir(), "anchor_counts.txt"))){
            HashMap<String, AnchorPartition> anchorMap = improvedDualClustering.getPartitions();
            writer.write("ID\tanchor_seq\tNum_Reads\tnumClusters\n");
            for (Map.Entry<String, AnchorPartition> entry : anchorMap.entrySet()){
                writer.write(entry.getValue().getClusterID()+"\t"+
                        entry.getKey()+"\t"+
                        entry.getValue().getCount()+"\t"+
                        entry.getValue().getCanonicalClusters().size()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Anchor Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.outDir(), "pos_mutations.txt"))){
            writer.write("Position\tNum_Corrected\n");
            for (int i = 0; i < Statistics.umiEdits.length; i++) {
                writer.write(i+"\t"+Statistics.umiEdits[i]+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Position Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.outDir(), "base_mutations.txt"))){
            writer.write("from\tto\tcount\n");
            for (Map.Entry<String, Integer> mutation : Statistics.mutations.entrySet()){
                writer.write(mutation.getKey().charAt(0)+"\t"+
                        mutation.getKey().charAt(1)+"\t"+
                        mutation.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Mutation Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.outDir(), "clusterHeaders.txt"))){
            writer.write("ID\theaders\n");
            for (Map.Entry<Integer, Set<String>> clusterEntry : improvedDualClustering.getClusterIDtoHeader().entrySet()){
                String result = String.join("|", clusterEntry.getValue());
                writer.write(clusterEntry.getKey()+"\t"+result+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing ClusterHeader Out");
        }

        System.out.println("Number of Reads: "+improvedDualClustering.getNumReads());
        System.out.println("Number of clusters: "+n);
        System.out.println("Number of anchor clusters: "+improvedDualClustering.getPartitions().size());
        System.out.println("largest Anchor Cluster size: "+Statistics.largestAnchorCluster);
        System.out.println("largest Umi Cluster size: "+Statistics.largestUmiCluster);
        System.out.println("largest Anchor Umi Cluster size: "+Statistics.largestUmiAnchorCluster);
    }
}
