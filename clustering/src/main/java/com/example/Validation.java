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

    private static String createMismatchMarker(String seq1, String seq2) {
        StringBuilder marker = new StringBuilder();
        int minLength = Math.min(seq1.length(), seq2.length());

        for (int i = 0; i < minLength; i++) {
            if (seq1.charAt(i) != seq2.charAt(i)) {
                marker.append('*');
            } else {
                marker.append(' ');
            }
        }

        // Handle length differences
        int maxLength = Math.max(seq1.length(), seq2.length());
        for (int i = minLength; i < maxLength; i++) {
            marker.append('*');
        }

        return marker.toString();
    }

    public static void validateSequences(String readFile, String outputFile, double mutRate){
        Hamming hamming = new Hamming();
        HashSet<String> set = new HashSet<>();
        try(BufferedReader br = new BufferedReader(new FileReader(readFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))){
            String line;
            while ((line = br.readLine()) != null){
                String[] split = line.split("\t");
                String umi = split[0];
                String seq = split[1];
                if (set.contains(seq)){
                    System.out.println("Duplicate line "+line);
                    continue;
                }
                for (String sequence: set){
                    if (hamming.compute(seq, sequence) <= mutRate){
                        String mismatchMarker = createMismatchMarker(seq, sequence);
                        bw.write("possible merge:"+"\n"+seq+"\n"+mismatchMarker+"\n"+sequence+"\n\n");
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
                            String mismatchMarker = createMismatchMarker(s, seq);
                            bw.write("possible merge:"+"\n"+s+"\n"+mismatchMarker+"\n"+seq+"\n\n");
                        }
                    }
                }
                else {
                    for (Map.Entry<String, Set<String>> entry: umi2seq.entrySet()){
                        if (umi.equals(entry.getKey())){continue;}
                        for (String s: entry.getValue()){
                            if(hamming.compute(s, seq) <= mutRate &&
                                    (int)(hamming.compute(entry.getKey(), umi) * umi.length()) <= 1){
                                String seqMismatchMarker = createMismatchMarker(s, seq);
                                String umiMismatchMarker = createMismatchMarker(entry.getKey(), umi);
                                bw.write("possible merge:"+"\n"+s+"\n"+seqMismatchMarker+"\n"+seq+"\n"+
                                        "cause lies also in umi:\n"+entry.getKey()+"\n"+umiMismatchMarker+"\n"+umi+"\n\n");
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
        String readFile = "/home/mojito/Desktop/Projects/Data/out/cluster.txt";
        String outputFile = "/home/mojito/Desktop/Projects/Data/out/validate.txt";
        double mutRate = 0.02;
        System.out.println((int)(mutRate * 150));
        validateSequences(readFile, outputFile, mutRate);
    }
}