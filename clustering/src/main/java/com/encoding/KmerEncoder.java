package com.encoding;

import com.kmer.KmerSet;
import com.model.Encoder;

public abstract class KmerEncoder<S extends KmerSet<S>> implements Encoder<String, S> {
    private final int k;

    protected KmerEncoder(int k) {
        this.k = k;
    }

    public int k() {
        return k;
    }


    public abstract S encode(String sequence);

}
