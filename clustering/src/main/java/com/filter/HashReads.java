package com.filter;

public class HashReads {

    byte[] phred;
    String header;
    String hash;
    String seq;

    public HashReads(String header, String seq, byte[] phred) {
        this.hash = seq.substring(seq.length()-50);
        this.seq = seq;
        this.phred = phred;
        this.header = header;
    }
}
