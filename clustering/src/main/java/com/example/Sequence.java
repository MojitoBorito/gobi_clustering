package com.example;

import com.model.Element;

public class Sequence extends Element<String> {
    byte[] phredSequence;

    public Sequence(String header, String sequence, byte[] phredSequence) {
        super(header, sequence);
        this.phredSequence = phredSequence;
    }

    public byte[] getPhredSequence() {
        return phredSequence;
    }

    static void main() {

    }
}
