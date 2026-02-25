package com.example;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class Main {
    static void main() {
        long start = System.currentTimeMillis();
        System.out.println("Reading fastq file...");
        UMI fastq = new UMI("/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H1-12936-T2_R2_001.fastq.gz");
        System.out.println("Counting UMIs...");
        long end = System.currentTimeMillis();
        long readTime = end - start;

        HashSet<String> umiSet = new HashSet<>();

        System.out.println("Reading time: "+ (readTime/1000)+" s");


        start = System.currentTimeMillis();
        fastq.filterUMI();
        end = System.currentTimeMillis();
        long filterTime = end - start;
        System.out.println("Filtering time: "+ (filterTime/1000)+" s");

        System.out.println("Number of UMIS: " + fastq.header2Umis.size());

        long uniqueConsensus = fastq.header2Umis.values().stream()
                .map(umi -> new String(umi.consensus, StandardCharsets.US_ASCII))
                .distinct()
                .count();
        System.out.println("Number of unique UMis: "+ uniqueConsensus);

    }
}
