package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {
    static void main(String[] args) {
        CmdParser cmdParser = new CmdParser("-umi", "-h", "-probs", "-counts", "-f");
        cmdParser.setFile("-umi");
        cmdParser.setInt("-h");
        cmdParser.setFile("-probs");
        cmdParser.setInt("-counts");
        cmdParser.setSwitches("-f");
        cmdParser.parse(args);

        String file = cmdParser.getValue("-umi");
        int thresh = cmdParser.getInt("-h");
        String probs = cmdParser.getValue("-out");
        String counts = cmdParser.getValue("-counts");
        boolean filter = cmdParser.isSet(".f");

        UMI fastq = new UMI(file, thresh);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(probs))){
            writer.write("header\tseq\n");
            for (String header : fastq.problematicUmis.keySet()){
                writer.write(header+"\t"+fastq.problematicUmis.get(header)+"\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(counts))){
            writer.write("seq\tcounts\n");
            for (Map.Entry<UMIseq, Integer> entry : fastq.counts.entrySet()){
                writer.write(new String(entry.getKey().consensus, StandardCharsets.US_ASCII)+
                        "\t"+entry.getValue()+"\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
