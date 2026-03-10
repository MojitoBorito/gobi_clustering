package com.benchmark;
import com.encoding.KmerLongSetEncoder;
import com.kmer.KmerLongSet;
import com.metrics.Hamming;
import com.metrics.Jaccard;
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
    private KmerLongSet kmersA, kmersB;
    private Hamming hamming = new Hamming();
    private Jaccard<KmerLongSet> jaccard = new Jaccard<>();
    private String[] candidateSeqs;
    private KmerLongSet[] candidateKmers;
    int N_CANDIDATES;

    @Setup
    public void setup() {
        candidateSeqs = new String[N_CANDIDATES];
        candidateKmers = new KmerLongSet[N_CANDIDATES];
        KmerLongSetEncoder enc = new KmerLongSetEncoder(17);
        for (int i = 0; i < N_CANDIDATES; i++) {
            String seq = SequenceUtils.randomSequence(150, 0);
            candidateSeqs[i] = seq;
            candidateKmers[i] = enc.encode(candidateSeqs[i]).minHash(8);
        }

    }

    @Benchmark
    public double stringHamming() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < N_CANDIDATES; i++) {
            min = Math.min(min, hamming.compute(seqA, candidateSeqs[i]));
        }
        return min;
    }

    @Benchmark
    public double jaccard() {
        return jaccard.compute(kmersA, kmersB);
    }
}