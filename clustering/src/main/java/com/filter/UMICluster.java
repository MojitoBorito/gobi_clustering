package com.filter;

import com.example.Statistics;

import java.util.Objects;


public class UMICluster {
    String seq;
    int n;
    byte[] phred;

    public UMICluster(String sequence, byte[] phredScore) {
        this.seq = sequence;
        this.phred = new byte[phredScore.length];
        for (int i = 0; i < phredScore.length; i++) {
            phred[i] = phredScore[i];
        }
        this.n=1;
    }
    
    public void updatePhred(byte[] phred){
        for (int i = 0; i < phred.length; i++) {
            this.phred[i] = (byte) (((this.phred[i]*n) + phred[i]) / (n+1));
        }
        this.n++;
        Statistics.incrementLargestUmiCluster(n, seq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UMICluster other) {
            return seq.equals(other.seq);
        }
        return false;
    }

    public String getSeq() {
        return seq;
    }

    public int getN() {
        return n;
    }
}
