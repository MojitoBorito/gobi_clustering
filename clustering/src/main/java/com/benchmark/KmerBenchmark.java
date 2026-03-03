package com.benchmark;
import com.kmer.*;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.infra.Blackhole;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class KmerBenchmark {
    private KmerHashSet<Long> set1;
    private KmerHashSet<Long> set2;
    private KmerBitEncoder encoder = new KmerBitEncoder(17);

    @Setup(Level.Trial)
    public void setup() {
        set1 = encoder.encode(generateSequence(200));
        set2 = encoder.encode(generateSequence(200));
    }

    @Benchmark
    public void intersectKmers(Blackhole bh) {
        bh.consume(set1.intersectSize(set2));
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
