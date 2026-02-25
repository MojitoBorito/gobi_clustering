package com.example;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    static void main() {
        long start = System.currentTimeMillis();
        System.out.println("Reading fastq file...");
        UMI fastq = new UMI("/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R2_001.fastq.gz");
        System.out.println("Counting UMIs...");
        long end = System.currentTimeMillis();
        long readTime = end - start;

        start = System.currentTimeMillis();
        System.out.println("Writing output file...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/mnt/biocluster/praktikum/genprakt/patil/Blockteil/umi_counts_grouped.tsv"))) {
            writer.write("umi\tcount\n");
            for (UMIseq umi: fastq.umis.keySet()) {
                writer.write(String.format("%s\t%d\n", new String(umi.seq, StandardCharsets.US_ASCII), umi.headers.size()));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        long writeTime = end - start;

        System.out.println("Reading time: "+ (readTime/1000)+" s");
        System.out.println("Writing time: "+ (writeTime/1000)+" s");

    }
}
