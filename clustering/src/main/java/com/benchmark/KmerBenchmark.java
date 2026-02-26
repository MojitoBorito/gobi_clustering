package com.benchmark;
import com.kmer.KmerBitEncoder;
import com.kmer.KmerEncoder;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class KmerBenchmark {
    private String sequence;
    private KmerEncoder<Long> encoder = new KmerBitEncoder(17);

    @Setup(Level.Trial)
    public void setup() {
        sequence = generateSequence(10000);
    }

    @Benchmark
    public void encodeKmers() {
        encoder.encode(sequence);
    }


    private String generateSequence(int length) {
        String bases = "ATCG";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(bases.charAt(i % 4));
        }
        return sb.toString();
    }
}
