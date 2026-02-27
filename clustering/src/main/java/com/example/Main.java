package com.example;

import com.filter.UMI;
import com.filter.UMICluster;

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

        UMI fastq = new UMI(file);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(counts))){
            writer.write("seq\tcounts\n");
            for (UMICluster seq : fastq.getUmis().values()){
                writer.write(new String(seq.getSeq(), StandardCharsets.US_ASCII)+
                        "\t"+seq.getN()+"\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
