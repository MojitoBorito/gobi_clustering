package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.metrics.Hamming;

public class Validation {

    public static void validateSequences(String readFile, String outputFile, double mutRate){
        Hamming hamming = new Hamming();
        HashSet<String> set = new HashSet<>();
        try(BufferedReader br = new BufferedReader(new FileReader(readFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))){
            String line;
            while ((line = br.readLine()) != null){
                if (set.contains(line)){
                    System.out.println("Duplicate line "+line);
                    continue;
                }
                for (String sequence: set){
                    if (hamming.compute(line,  sequence) <= mutRate){
                        bw.write("possible merge:"+"\n"+line+"\n"+sequence+"\n\n");
                    }
                }
                set.add(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void validateSequencesUMI(String readFile, String outputFile, double mutRate){
        Hamming hamming = new Hamming();
        HashMap<String, Set<String>> umi2seq = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(readFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))){
            String line;
            while ((line = br.readLine()) != null){
                String[] sequence = line.split("\t");
                String umi = sequence[0];
                String seq = sequence[1];
                if(umi.equals("umi") && seq.equals("seq")){
                    continue;
                } 
                if (umi2seq.containsKey(umi)){
                    Set<String> set = umi2seq.get(umi);
                    for (String s: set){
                        if(hamming.compute(s, seq) <= mutRate){
                            bw.write("possible merge:"+"\n"+s+"\n"+seq+"\n\n");
                        }
                    }
                }else {
                    for (Map.Entry<String, Set<String>> entry: umi2seq.entrySet()){
                        for (String s: entry.getValue()){
                            if(hamming.compute(s, seq) <= mutRate &&
                                    (int)(hamming.compute(entry.getKey(), umi) * umi.length()) <= 1){
                                bw.write("possible merge:"+"\n"+s+"\n"+seq+"\n"+
                                        "cause lies also in umi:\n"+entry.getKey()+"\n"+umi+"\n\n");
                            }
                        }
                    }
                    umi2seq.computeIfAbsent(umi, _ -> new HashSet<>()).add(seq);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String readFile = "/mnt/biocluster/praktikum/genprakt/patil/Blockteil/dual_out2/clusters.txt";
        String outputFile = "/mnt/biocluster/praktikum/genprakt/patil/Blockteil/dual_out2/validate.txt";
        double mutRate = 0.02;
        System.out.println((int)(mutRate * 150));
        validateSequencesUMI(readFile, outputFile, mutRate);
    }
}
