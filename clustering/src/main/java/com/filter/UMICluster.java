package com.filter;

import java.util.Arrays;
import java.util.HashMap;


public class UMICluster {
    byte[] seq;
    int n;

    HashMap<SeqKey, SubCluster> sub50cluster = null;

    public UMICluster(byte[] sequence) {
        this.seq = sequence;
        n=1;
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
