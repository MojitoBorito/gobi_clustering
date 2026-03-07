package com.example;

import com.filter.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    static void main(String[] args) {
        CmdParser cmdParser = new CmdParser("-umi", "-reads", "-outCluster");
        cmdParser.setFile("-umi", "-outCluster", "-reads");
        cmdParser.parse(args);

        String umi = cmdParser.getValue("-umi");
        String umiOut = cmdParser.getValue("-outCluster");

        String fw = cmdParser.getValue("-reads");

        Statistics.umiEdits = new int[12];
        Statistics.AnchorEdits = new int[150];

        long starTime = System.currentTimeMillis();

        UMI umiGroup = new UMI(umi);

        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        ImprovedDualClustering improvedDualClustering = new ImprovedDualClustering(umiGroup, fw);

        endTime = System.currentTimeMillis();
        long second = endTime - starTime;

        System.out.println("umi clustering time: "+first);
        System.out.println("Dual clustering time: "+second);

        int n = 0;
        List<CorrectedUMICluster> clusters = improvedDualClustering.getClusters();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(umiOut))){
            writer.write("umi\tseq\tcounts\n");
            for (CorrectedUMICluster cluster : clusters){
                    writer.write(cluster.getUmi()+"\t"+cluster.getRead()+"\t"+cluster.getCount()+"\n");
                    n++;

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        try(){
//
//        }

        System.out.println("Number of clusters: "+n);
        System.out.println("Number of anchor clusters: "+improvedDualClustering.getPartitions().size());
        System.out.println("largest Anchor Cluster size: "+Statistics.largestAnchorCluster);
        System.out.println("largest Umi Cluster size: "+Statistics.largestUmiCluster);
        System.out.println("largest Anchor Umi Cluster size: "+Statistics.largestUmiAnchorCluster);
    }
}
