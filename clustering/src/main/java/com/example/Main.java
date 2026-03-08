package com.example;

import com.cli.CliParser;
import com.cli.CmdOptions;
import com.filter.*;
import org.apache.commons.cli.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

        writeOutput(options, improvedDualClustering);
    }


    public static void writeOutput(CmdOptions options, ImprovedDualClustering improvedDualClustering) {
        int n = 0;
        List<CorrectedUMICluster> clusters = improvedDualClustering.getClusters();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(options.getOutCluster()))) {
            writer.write("UMI\tseq\tcounts\n");
            for (CorrectedUMICluster cluster : clusters){
                writer.write(cluster.getUmi()+"\t"+cluster.getRead()+"\t"+cluster.getCount()+"\n");
                n++;

            }
        } catch (IOException e) {
            throw new RuntimeException("Error at writing Cluster Out");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(options.getOutUmi()))){
            HashMap<String, Integer> correctedUmis = improvedDualClustering.getCorrectedUmis();
            writer.write("corrected_UMI\tcounts\n");
            for (Map.Entry<String, Integer> entry : correctedUmis.entrySet()){
                writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Umi Out");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(options.getOutAnchor()))){
            HashMap<String, AnchorPartition> anchorMap = improvedDualClustering.getPartitions();
            writer.write("anchor_seq\tNum_Reads\tnumClusters\n");
            for (Map.Entry<String, AnchorPartition> entry : anchorMap.entrySet()){
                writer.write(entry.getKey()+"\t"+
                        entry.getValue().getCount()+"\t"+
                        entry.getValue().getCanonicalClusters().size()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Anchor Out");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(options.getOutPos()))){
            writer.write("Position\tNum_Corrected\n");
            for (int i = 0; i < Statistics.umiEdits.length; i++) {
                writer.write(i+"\t"+Statistics.umiEdits[i]+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Position Out");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(options.getOutMut()))){
            writer.write("from\tto\tcount\n");
            for (Map.Entry<String, Integer> mutation : Statistics.mutations.entrySet()){
                writer.write(mutation.getKey().charAt(0)+"\t"+
                        mutation.getKey().charAt(1)+"\t"+
                        mutation.getValue()+"\n");
            }
        }catch (IOException e) {
            throw new RuntimeException("Error at writing Mutation Out");
        }

        System.out.println("Number of clusters: "+n);
        System.out.println("Number of anchor clusters: "+improvedDualClustering.getPartitions().size());
        System.out.println("largest Anchor Cluster size: "+Statistics.largestAnchorCluster);
        System.out.println("largest Umi Cluster size: "+Statistics.largestUmiCluster);
        System.out.println("largest Anchor Umi Cluster size: "+Statistics.largestUmiAnchorCluster);
    }
}
