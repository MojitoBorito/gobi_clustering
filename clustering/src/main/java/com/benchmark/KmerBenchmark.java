package com.benchmark;
import com.kmer.*;
import com.metrics.Hamming;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.infra.Blackhole;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class KmerBenchmark {
    String seq1;
    String seq2;
    Hamming dist = new Hamming();

    @Setup(Level.Trial)
    public void setup() {
        seq1 = generateSequence(150);
        seq2 = generateSequence(150);
    }

    @Benchmark
    public void intersectKmers(Blackhole bh) {bh.consume(dist.compute(seq1, seq2));
    }


    private String generateSequence(int length) {
        char[] bases = {'A','C','G','T'};
        java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(bases[rnd.nextInt(4)]);
        }
        return sb.toString();
    }
}
