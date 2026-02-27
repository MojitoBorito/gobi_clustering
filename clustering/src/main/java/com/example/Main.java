package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    static void main(String[] args) {
        CmdParser cmdParser = new CmdParser("-umi", "-counts");
        cmdParser.setFile("-umi");
        cmdParser.setInt("-counts");
        cmdParser.parse(args);

        String file = cmdParser.getValue("-umi");
        String counts = cmdParser.getValue("-counts");

        BaseCluster fastq = new BaseCluster(file);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(counts))){
            writer.write("seq\tcounts\n");
            for (BaseClusterSeq seq : fastq.umis.values()){
                writer.write(new String(seq.seq, StandardCharsets.US_ASCII)+
                        "\t"+seq.n+"\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
