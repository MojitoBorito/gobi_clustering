package com.example;

import com.metrics.Hamming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                    if (hamming.compute(line,  sequence) <= (int)(mutRate * sequence.length())){
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
                if (umi2seq.containsKey(umi)){
                    Set<String> set = umi2seq.get(umi);
                    for (String s: set){
                        if(hamming.compute(s, seq) <= (int)(mutRate * seq.length())){
                            bw.write("possible merge:"+"\n"+line+"\n"+seq+"\n\n");
                        }
                    }
                }else {
                    for (Map.Entry<String, Set<String>> entry: umi2seq.entrySet()){
                        for (String s: entry.getValue()){
                            if(hamming.compute(s, seq) <= (int)(mutRate * seq.length()) &&
                                    hamming.compute(entry.getKey(), umi) <= (int)(mutRate * seq.length())){
                                bw.write("possible merge:"+"\n"+line+"\n"+seq+"\n"+
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
}
