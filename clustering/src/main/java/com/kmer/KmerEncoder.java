package com.kmer;

public abstract class KmerEncoder{
    private final int k;

    protected KmerEncoder(int k) {
        this.k = k;
    }

    public int k() {
        return k;
    }

    public abstract KmerSet encode(String sequence);

}
