package com.example;

import com.filter.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    static void main(String[] args) {
        CmdParser cmdParser = new CmdParser("-umi", "-out", "-reads");
        cmdParser.setFile("-umi", "-out", "-reads");
        cmdParser.parse(args);

        String umi = cmdParser.getValue("-umi");
        String umiOut = cmdParser.getValue("-out");

        String fw = cmdParser.getValue("-reads");

        long starTime = System.currentTimeMillis();

        UMI umiGroup = new UMI(umi);

        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        ImprovedDualClustering improvedDualClustering = new ImprovedDualClustering(umiGroup, fw);

        endTime = System.currentTimeMillis();
        long second = endTime - starTime;
        int n = 0;
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(umiOut))){
            writer.write("umi\tseq\tcounts\n");
            for (AnchorPartition reads : improvedDualClustering.getPartitions().values()){
                for (CorrectedUMICluster cluster : reads.getUmiMap().values()){
                    writer.write(cluster.getUmi()+"\t"+cluster.getRead()+"\t"+cluster.getCount()+"\n");
                    n++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("umi clustering time: "+first);
        System.out.println("Dual clustering time: "+second);
        System.out.println("Number of clusters: "+n);
    }
}
