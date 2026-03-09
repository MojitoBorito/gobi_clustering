package com.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class HammingBenchmark {

    private String seqA, seqB;
    private long[] bitsA, bitsB;

    @Setup
    public void setup() {
        // generate two random 150bp sequences with ~3% divergence
        char[] bases = {'A', 'C', 'G', 'T'};
        Random rand = new Random(42);
        char[] a = new char[150];
        char[] b = new char[150];
        for (int i = 0; i < 150; i++) {
            a[i] = bases[rand.nextInt(4)];
            // 3% chance of mismatch
            b[i] = rand.nextDouble() < 0.03 ? bases[rand.nextInt(4)] : a[i];
        }
        seqA = new String(a);
        seqB = new String(b);
//        bitsA = BitsetEncoder.encode(seqA);
//        bitsB = BitsetEncoder.encode(seqB);
    }

    @Benchmark
    public double stringHamming() {
        int dist = 0;
        for (int i = 0; i < seqA.length(); i++) {
            dist += (seqA.charAt(i) == seqB.charAt(i)) ? 0 : 1;
        }
        return (double) dist / 150;
    }

    @Benchmark
    public double bitsetHamming() {
        int dist = 0;
        for (int i = 0; i < bitsA.length; i++) {
            long xor = bitsA[i] ^ bitsB[i];
            long odd  = (xor >>> 1) & 0x5555555555555555L;
            long even =  xor        & 0x5555555555555555L;
            dist += Long.bitCount(odd | even);
        }
        return (double) dist / 150;
    }
}