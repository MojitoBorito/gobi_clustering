package com.example;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class Main {
    static void main() {
        long start = System.currentTimeMillis();
        System.out.println("Reading fastq file...");
        UMI fastq = new UMI("/home/mojito/Desktop/Projects/Data/H1-12936-T2_R2_001.fastq.gz");
        System.out.println("Counting UMIs...");
        long end = System.currentTimeMillis();
        long readTime = end - start;

        start = System.currentTimeMillis();
        System.out.println("Writing output file...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/home/mojito/Desktop/Projects/Data/out/umi_counts_grouped_new.tsv"))) {
            writer.write("umi\tcount\n");
            for (UMIseq umi: fastq.umis.keySet()) {
                writer.write(String.format("%s\t%d\n", new String(umi.consensus, StandardCharsets.US_ASCII), umi.headers.size()));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        long writeTime = end - start;

        HashSet<String> umiSet = new HashSet<>();
        for (UMIseq umi: fastq.umis.keySet()) {
            if (!umiSet.add(new String(umi.consensus, StandardCharsets.US_ASCII))) {
                System.out.println(new String(umi.consensus, StandardCharsets.US_ASCII));
            }
        }

        System.out.println("Reading time: "+ (readTime/1000)+" s");
        System.out.println("Writing time: "+ (writeTime/1000)+" s");

    }
}
