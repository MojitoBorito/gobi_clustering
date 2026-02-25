package com.example;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class UMIseq {
    byte[] seq;
    int hash;
    byte[] consensus;

    int[][] scores;

    private static final byte[] INDEX_TO_BASE = {'A', 'C', 'G', 'T', 'N'};

    public UMIseq (String sequence, boolean real) {
        seq = sequence.getBytes(StandardCharsets.US_ASCII);
        this.hash = fnv1a(seq);
        if (real){
            scores = new int[5][seq.length];
            consensus = Arrays.copyOf(seq, seq.length);
        }
    }

    public void makeStructures(){
        scores = new int[5][seq.length];
        consensus = Arrays.copyOf(seq, seq.length);
    }

    public void addSequence(byte[] seq, byte[] phred, String header){
        updateScore(seq, phred);
        updateSequence();
    }

    public void updateScore(byte[] seq, byte[] phred){
        int index;
        for (int i = 0; i < seq.length; i++){
            index = baseToIndex(seq[i]);
            scores[index][i] = phred[i];
        }
    }

    public void updateSequence(){
        int bestIndex;
        int bestScore;
        for (int i = 0; i < consensus.length; i++){
            bestIndex = 0;
            bestScore = scores[0][i];
            for (int j = 1; j < 4; j++){
                if (scores[j][i] > bestScore){
                    bestIndex = j;
                    bestScore = scores[j][i];
                }
            }
            if (bestScore > 0){
                consensus[i] = INDEX_TO_BASE[bestIndex];
            } else {
                consensus[i] = 'N';
            }
        }
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

    private static int baseToIndex(byte base) {
        switch (base) {
            case 'A':
            case 'a':
                return 0;
            case 'C':
            case 'c':
                return 1;
            case 'G':
            case 'g':
                return 2;
            case 'T':
            case 't':
                return 3;
            default:
                return 4;
        }
    }
}
