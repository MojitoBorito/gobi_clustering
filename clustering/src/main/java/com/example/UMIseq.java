package com.example;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;


public class UMIseq {
    byte[] seq;
    int hash;

    HashMap<String, byte[]> sequences; // maps header to sequence
    HashMap<String, byte[]> phredScores; // maps header to phred score string
    //int[][] scores;


    public UMIseq (String sequence, boolean real) {
        seq = sequence.getBytes(StandardCharsets.US_ASCII);
        this.hash = fnv1a(seq);
        if (real){
            sequences = new HashMap<>();
            phredScores = new HashMap<>();
            //scores = new int[5][seq.length];
        }
    }

    public void makeStructures(){
        sequences = new HashMap<>();
        phredScores = new HashMap<>();
    }

    public void addSequence(byte[] seq, byte[] phred, String header){
        sequences.put(header, seq);
        phredScores.put(header, phred);
    }

    public static int fnv1a(byte[] data) {
        int hash = 0x811c9dc5;
        for (int i = 0; i < data.length; i++) {
            hash ^= data[i];
            hash *= 0x01000193;
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UMIseq other) {
            return Arrays.equals(seq, other.seq);
        }
        return false;
    }
}
