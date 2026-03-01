package com.filter;

import java.util.Arrays;
import java.util.HashMap;


public class UMICluster {
    byte[] seq;
    int n;
    int[] phred;

    HashMap<SeqKey, SubCluster> sub50cluster = null;

    public UMICluster(byte[] sequence, byte[] phredScore) {
        this.seq = sequence;
        this.phred = new int[phredScore.length];
        for (int i = 0; i < phredScore.length; i++) {
            phred[i] = phredScore[i];
        }
        this.n=1;
    }
    
    public void updatePhred(byte[] phred){
        for (int i = 0; i < phred.length; i++) {
            this.phred[i] = ((this.phred[i]*n) + phred[i]) / (n+1);
        }
        this.n++;
    }

    @Override
    public int hashCode() {
        return SeqKey.fnv1a(seq);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UMICluster other) {
            return Arrays.equals(seq, other.seq);
        }
        return false;
    }

    public byte[] getSeq() {
        return seq;
    }

    public int getN() {
        return n;
    }
}
