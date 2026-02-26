package com.example;

import java.util.Arrays;
import java.util.HashMap;


public class BaseClusterSeq {
    byte[] seq;
    int n;

    HashMap<SeqKey, SubCluster> sub50cluster = null;

    public BaseClusterSeq(byte[] sequence) {
        this.seq = sequence;
        n=1;
    }

    @Override
    public int hashCode() {
        return SeqKey.fnv1a(seq);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseClusterSeq other) {
            return Arrays.equals(seq, other.seq);
        }
        return false;
    }

}
