package com.filter;

public class HashReads {

    byte[] phred;
    String header;
    SeqKey hash;
    byte[] seq;

    public HashReads(String header, byte[] seq, byte[] phred) {
        this.hash = new SeqKey(seq, 50);
        this.seq = seq;
        this.phred = phred;
        this.header = header;
    }
}
