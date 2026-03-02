package com.example;

import com.filter.*;

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

        long starTime = System.currentTimeMillis();

        UMI umiGroup = new UMI(umi);

        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        DualClustering dualClustering = new DualClustering(umiGroup, fw);

        endTime = System.currentTimeMillis();
        long second = endTime - starTime;

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(umiOut))){
            writer.write("seq\tcounts\n");
            for (ReadCluster reads : dualClustering.getReadClustering().getClusters().values()){
                SubCluster seq = reads.getUmis();
                seq.updateSequence();
                writer.write(new String(seq.getConsensus(), StandardCharsets.US_ASCII) + "\t" + seq.getN() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fwOut))){
            writer.write("seq\tcounts\n");
            for (SubCluster seq : dualClustering.getUmiClustering().getSubClusters().values()){
                seq.updateSequence();
                writer.write(new String(seq.getConsensus(), StandardCharsets.US_ASCII) + "\t" + seq.getN() + "\n");
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println("Umi cluster time: "+(first/1000));
        System.out.println("Dual cluster time: "+(second/1000));

    }
}
