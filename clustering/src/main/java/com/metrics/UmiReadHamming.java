package com.metrics;

import com.model.UmiRead;

public class UmiReadHamming implements DistanceMetric<UmiRead>{
    private final Hamming hamming;

    public UmiReadHamming(int exitAfter) {
        this.hamming = new Hamming(exitAfter);
    }

    public UmiReadHamming() {this.hamming = new Hamming();}

    @Override
    public double compute(UmiRead e1, UmiRead e2) {
        return hamming.compute(e1.sequence(), e2.sequence());
    }
}
