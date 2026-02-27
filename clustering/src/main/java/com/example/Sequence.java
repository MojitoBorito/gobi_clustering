package com.example;

public class Sequence {
    String header;
    byte[] sequence;
    byte[] phredSequence;

    public Sequence(String header, byte[] sequence, byte[] phredSequence) {
        this.header = header;
        this.sequence = sequence;
        this.phredSequence = phredSequence;
    }

    public byte[] getSequence() {
        return sequence;
    }
}
