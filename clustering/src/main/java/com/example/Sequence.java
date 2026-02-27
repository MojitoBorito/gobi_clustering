package com.example;

public class Sequence {
    String header;
    String sequence;
    String phredSequence;

    public Sequence(String header, String sequence, String phredSequence) {
        this.header = header;
        this.sequence = sequence;
        this.phredSequence = phredSequence;
    }

    public String getSequence() {
        return sequence;
    }

    public String getHeader() {
        return header;
    }

    public String getPhredSequence() {
        return phredSequence;
    }
}
