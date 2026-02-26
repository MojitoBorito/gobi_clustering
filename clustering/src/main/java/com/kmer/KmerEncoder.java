package com.kmer;

public abstract class KmerEncoder<E> {
    private final int k;

    protected KmerEncoder(int k) {
        this.k = k;
    }

    public int k() {
        return k;
    }
    public abstract KSet<E> encode(String sequence);
}
