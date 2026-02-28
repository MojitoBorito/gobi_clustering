package com.filter;

public class HashReads extends SeqKey{

    byte[] phred;
    String header;

    public HashReads( String header, byte[] seq, byte[] phred) {
        super(seq, 50);
        this.phred = phred;
        this.header = header;
    }
}
