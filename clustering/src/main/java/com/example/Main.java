package com.example;

import com.filter.DualClustering;
import com.filter.SubCluster;
import com.filter.UMI;
import com.filter.UMICluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    static void main(String[] args) {
        CmdParser cmdParser = new CmdParser("-umi", "-umiOut", "-reads", "-readsOut");
        cmdParser.setFile("-umi", "-umiOut", "-reads", "-readsOut");
        cmdParser.parse(args);

        String umi = cmdParser.getValue("-umi");
        String umiOut = cmdParser.getValue("-umiOut");

        String fw = cmdParser.getValue("-reads");
        String fwOut = cmdParser.getValue("-readsOut");

        UMI umiGroup = new UMI(umi);

        DualClustering dualClustering = new DualClustering(umiGroup, fw);

        long starTime = System.currentTimeMillis();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(umiOut))){
            writer.write("seq\tcounts\n");
            for (SubCluster seq : dualClustering.getSeqClusters()){
                seq.updateSequence();
                writer.write(new String(seq.getConsensus(), StandardCharsets.US_ASCII) + "\t" + seq.getN() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long endTime = System.currentTimeMillis();

        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fwOut))){
            writer.write("seq\tcounts\n");
            for (SubCluster seq : dualClustering.getUmiClusters()){
                seq.updateSequence();
                writer.write(new String(seq.getConsensus(), StandardCharsets.US_ASCII) + "\t" + seq.getN() + "\n");
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        endTime = System.currentTimeMillis();

        long second = endTime - starTime;

        System.out.println("Umi cluster time: "+(first/1000));
        System.out.println("Dual cluster time: "+(second/1000));

    }
}
