package com.example;

import com.bucket.PosKmerBuckets;
import com.bucket.UmiAwareBuckets;
import com.bucket.UmiKey;
import com.cli.CliParser;
import com.cli.CmdOptions;
import com.clustering.ClusteringAlgorithm;
import com.clustering.GreedyClusters;
import com.clustering.MohitParser;
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
        // READ UMIS
        long starTime = System.currentTimeMillis();
        UMI umiGroup = new UMI(options.umi());
        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;
        System.out.println("umi clustering time: "+ first / (1000.0) + " sec");
//        System.out.print(first / (1000.0) + "\t");
        // GENERATE CLUSTERS
        starTime = System.currentTimeMillis();
        ImprovedDualClustering improvedDualClustering = new ImprovedDualClustering(umiGroup, options.reads());
        endTime = System.currentTimeMillis();
        long second = endTime - starTime;
        System.out.println("Dual clustering time: "+ second / (1000.0 * 60) + " min" +"\n");
//        System.out.print(second / (1000.0) + "\t");

        // Needed for second clustering cycle
        HashMap<Integer, Set<String>> subClusterIDToHeader= improvedDualClustering.getClusterIDtoHeader();

        // OUTPUT PATHS
        Path primaryClusteringPath = Path.of(options.outDir(), "clusters.txt");
        Path secondaryClusteringPath = Path.of(options.outDir(), "secondary_clusters.txt");
        Path umiCountsPath = Path.of(options.outDir(), "umi_counts.txt");
        Path anchorCountsPath = Path.of(options.outDir(), "anchor_counts.txt");
        Path mutationsPath = Path.of(options.outDir(), "pos_mutations.txt");
        Path baseMutationsPath = Path.of(options.outDir(), "base_mutations.txt");
        Path clusterHeadersPath = Path.of(options.outDir(), "clusterHeaders.txt");

        // WRITE OUTPUT/STATS
        writePrimaryOutputs(primaryClusteringPath, umiCountsPath, anchorCountsPath, mutationsPath, baseMutationsPath, clusterHeadersPath, improvedDualClustering, umiGroup);


        // CLEAN UP
        improvedDualClustering = null;
        System.gc();

        // FINER CLUSTERING
        if (options.runSecondaryClustering()) {
            // FINER CLUSTER GENERATION
//            System.out.println("Starting finer clustering...");
            long start = System.currentTimeMillis();
            ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm = runWithUmis(primaryClusteringPath, options.readLength(), options.kmerSize(), options.threshold());
            long finerClusteringEnd = System.currentTimeMillis();
            System.out.println("Finer clustering generation time: " + ((finerClusteringEnd - start) / (1000.0*60)) + " min");
            System.out.println("Writing finer cluster output...");
//            System.out.println((finerClusteringEnd - start)/(1000.0) + "\t"+ (first + second + (finerClusteringEnd - start))/(1000.0));

            // FINER CLUSTERING OUTPUT
            long outputStart = System.currentTimeMillis();
            Set<SeededCluster<UmiRead>> computedClusters = algorithm.getAllClusters();
            writeSecondaryCycleOutput(secondaryClusteringPath, computedClusters, subClusterIDToHeader);
            long outputEnd = System.currentTimeMillis();
            System.out.println("Finer clustering write time: " + ((outputEnd - outputStart) / (1000.0)) + " ms");
            System.out.println("Total finer clustering time: " + (outputEnd - start) / (1000.0 * 60) + " s");
        }
    }

    private static ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> runWithUmis(Path pathtoUmiClusters, int readLength, int kmerSize, double threshold) {
        try {
            MohitParser mohit = new MohitParser(pathtoUmiClusters);
            ClusteringAlgorithm<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm = initUmiAwareAlgorithm(readLength, kmerSize, threshold);
            algorithm.computeClusters(mohit.iterator());
            return algorithm;
        } catch (Exception e) {
            throw new RuntimeException("Error reading primary clustering output!", e);
        }
    }

    private static void writeSecondaryCycleOutput(Path outputPath,
                                                  Set<SeededCluster<UmiRead>> computedClusters,
                                                  HashMap<Integer,
                                                          Set<String>> subClusterIDToHeader) {
        long outputStart = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toString()))) {
            writer.write("cluster_id");
            writer.write('\t');
            writer.write("read_id");
            writer.write('\n');
            for (SeededCluster<UmiRead> cluster : computedClusters) {
                for (String subClusterId : cluster.getElementIds()) {
                    for (String readId : subClusterIDToHeader.get(Integer.parseInt(subClusterId))) {
                        writer.write(String.valueOf(cluster.getId()));
                        writer.write('\t');
                        writer.write(readId);
                        writer.write('\n');
                    }
                }
            }
            long outputEnd = System.currentTimeMillis();
        } catch (Exception e) {
            throw new RuntimeException("Error writing finer clusters", e);
        }
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

    private static void writePrimaryOutputs(
            Path primaryClusteringPath,
            Path umiCountsPath,
            Path anchorCountsPath,
            Path mutationsPath,
            Path baseMutationsPath,
            Path clusterHeadersPath,
            ImprovedDualClustering improvedDualClustering,
            UMI umis
    ) {
        // OUTPUT
        HashMap<String, Integer> correctedUmis = improvedDualClustering.getCorrectedUmis();
        HashMap<String, AnchorPartition> anchorMap = improvedDualClustering.getPartitions();
        HashMap<Integer, Set<String>> subClusterIDToHeader= improvedDualClustering.getClusterIDtoHeader();
        List<CorrectedUMICluster> correctedUMIClusters = improvedDualClustering.getClusters();
        writePrimaryClusteringOutput(primaryClusteringPath, correctedUMIClusters);
        writeUmiCounts(umiCountsPath, correctedUmis);
        writeAnchorCounts(anchorCountsPath, anchorMap);
        writeMutations(mutationsPath);
        writeBaseMutations(baseMutationsPath);
        writeClusterHeaders(clusterHeadersPath, subClusterIDToHeader);

        // STATS
        int numOfReads = improvedDualClustering.getNumReads();
        int numOfCluster = correctedUMIClusters.size();
        int numOfAnchorClusters = improvedDualClustering.getPartitions().size();
        int numberOfUncorrectedUmis = umis.getNumUmis();
        int numberOfCorrectedUmis = correctedUmis.size();
        printPrimaryClusteringLongs(numOfReads, numOfCluster, numOfAnchorClusters, numberOfUncorrectedUmis, numberOfCorrectedUmis);
    }

    private static void writePrimaryClusteringOutput(Path outputPath, List<CorrectedUMICluster> clusters) {
        int n = 0;
        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("ID\tUMI\tseq\tcounts\n");
            for (CorrectedUMICluster cluster : clusters){
                writer.write(cluster.getClusterID()+"\t"+cluster.getUmi()+"\t"+cluster.getRead()+"\t"+cluster.getCount()+"\n");
                n++;

            }
        } catch (IOException e) {
            throw new RuntimeException("Error at writing Cluster Out");
        }
    }

    private static void writeUmiCounts(Path outputPath, Map<String, Integer> correctedUmis) {
        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)){
            writer.write("corrected_UMI\tcounts\n");
            for (Map.Entry<String, Integer> entry : correctedUmis.entrySet()){
                writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Umi Out");
        }
    }

    private static void writeAnchorCounts(Path outputPath, Map<String, AnchorPartition> anchorMap) {
        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)){
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
    }

    private static void writeMutations(Path outputPath) {
        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)){
            writer.write("Position\tNum_Corrected\n");
            for (int i = 0; i < Statistics.umiEdits.length; i++) {
                writer.write(i+"\t"+Statistics.umiEdits[i]+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Position Out");
        }
    }

    private static void writeBaseMutations(Path outputPath) {
        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)){
            writer.write("from\tto\tcount\n");
            for (Map.Entry<String, Integer> mutation : Statistics.mutations.entrySet()){
                writer.write(mutation.getKey().charAt(0)+"\t"+
                        mutation.getKey().charAt(1)+"\t"+
                        mutation.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Mutation Out");
        }
    }

    private static void writeClusterHeaders(Path outputPath, Map<Integer, Set<String>> clusterIdToHeader) {
        try(BufferedWriter writer = Files.newBufferedWriter(outputPath)){
            writer.write("ID\theaders\n");
            for (Map.Entry<Integer, Set<String>> clusterEntry : clusterIdToHeader.entrySet()){
                String result = String.join("|", clusterEntry.getValue());
                writer.write(clusterEntry.getKey()+"\t"+result+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing ClusterHeader Out");
        }
    }

    private static void printPrimaryClusteringLongs(int numOfReads,
                                                    int numOfClusters,
                                                    int numOfAnchorClusters,
                                                    int numUncorrectedUmis,
                                                    int numCorrectedUmis) {
        System.out.println("Number of Reads: " + numOfReads);
        System.out.println("Number of clusters: " + numOfClusters);
        System.out.println("Number of uncorrected UMIs clusters: " + numUncorrectedUmis);
        System.out.println("Number of corrected UMIs: " + numCorrectedUmis);
        System.out.println("Number of anchor clusters: " + numOfAnchorClusters);
        System.out.println("largest Anchor Cluster size: "+Statistics.largestAnchorCluster);
        System.out.println("largest Umi Cluster size: "+Statistics.largestUmiCluster);
        System.out.println("largest Anchor Umi Cluster size: "+Statistics.largestUmiAnchorCluster);
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
