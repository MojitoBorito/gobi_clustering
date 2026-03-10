package com.example;

import com.bucket.UmiAwareBuckets;
import com.bucket.UmiKey;
import com.cli.CliParser;
import com.cli.CmdOptions;
import com.clustering.GreedyClusters;
import com.encoding.UmiPosKmerBitEncoder;
import com.filter.*;
import com.linkage.ClusterLinkage;
import com.linkage.SeededLinkage;
import com.metrics.DistanceMetric;
import com.metrics.UmiReadHamming;
import com.model.*;
import com.pipeline.ValueMappingIterator;
import com.seeds.AnchorSeed;
import org.apache.commons.cli.ParseException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static void main(String[] args) throws ParseException {
        CmdOptions options = CliParser.parse(args);

        Statistics.umiEdits = new int[12];
        Statistics.AnchorEdits = new int[150];

        long starTime = System.currentTimeMillis();

        UMI umiGroup = new UMI(options.getUmi());

        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        ImprovedDualClustering improvedDualClustering = new ImprovedDualClustering(umiGroup, options.getReads());

        endTime = System.currentTimeMillis();
        long second = endTime - starTime;

        System.out.println("umi clustering time: "+first);
        System.out.println("Dual clustering time: "+second);
        List<CorrectedUMICluster> umiClusters = improvedDualClustering.getClusters();


        writeOutput(options, improvedDualClustering, umiClusters);


        // Part 2
        runWithUmis(umiClusters, Paths.get(options.getOutDir(), "finer_clusters.txt"));
    }

    public static void runWithUmis(List<CorrectedUMICluster> umiClusters, Path output) {
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

        UmiAwareBuckets<SeededCluster<UmiRead>> buckets = new UmiAwareBuckets<>(5);
        SeededCluster.SeedFactory<UmiRead> seedFactory = AnchorSeed::new;
        Universe.ClusterFactory<SeededCluster<UmiRead>> clusterFactory = id -> new SeededCluster<>(id, seedFactory);
        DistanceMetric<UmiRead> metric = new UmiReadHamming(5);
        ClusterLinkage<UmiRead, SeededCluster<UmiRead>> linkage = new SeededLinkage<>();
        Encoder<UmiRead, UmiKey> encoder = new UmiPosKmerBitEncoder(30);
        double threshold = 0.03;

        GreedyClusters<UmiKey, UmiRead, SeededCluster<UmiRead>> algorithm =
                new GreedyClusters<>(buckets, clusterFactory, metric, linkage, encoder, threshold);

        algorithm.computeClusters(iteratorMap);
        algorithm.writeClustersCompact(output);
    }


    public static void writeOutput(CmdOptions options, ImprovedDualClustering improvedDualClustering, List<CorrectedUMICluster> clusters) {
        int n = 0;
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.getOutDir(), "clusters.txt"))) {
            writer.write("ID\tUMI\tseq\tcounts\n");
            for (CorrectedUMICluster cluster : clusters){
                writer.write(cluster.getClusterID()+"\t"+cluster.getUmi()+"\t"+cluster.getRead()+"\t"+cluster.getCount()+"\n");
                n++;

            }
        } catch (IOException e) {
            throw new RuntimeException("Error at writing Cluster Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.getOutDir(), "umi_counts.txt"))){
            HashMap<String, Integer> correctedUmis = improvedDualClustering.getCorrectedUmis();
            writer.write("corrected_UMI\tcounts\n");
            for (Map.Entry<String, Integer> entry : correctedUmis.entrySet()){
                writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Umi Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.getOutDir(), "anchor_counts.txt"))){
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

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.getOutDir(), "pos_mutations.txt"))){
            writer.write("Position\tNum_Corrected\n");
            for (int i = 0; i < Statistics.umiEdits.length; i++) {
                writer.write(i+"\t"+Statistics.umiEdits[i]+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Position Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.getOutDir(), "base_mutations.txt"))){
            writer.write("from\tto\tcount\n");
            for (Map.Entry<String, Integer> mutation : Statistics.mutations.entrySet()){
                writer.write(mutation.getKey().charAt(0)+"\t"+
                        mutation.getKey().charAt(1)+"\t"+
                        mutation.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Mutation Out");
        }

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(options.getOutDir(), "clusterHeaders.txt"))){
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
