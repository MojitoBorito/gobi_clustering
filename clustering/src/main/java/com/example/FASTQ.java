package com.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class FASTQ {
    HashMap<String, Sequence> fastq = new HashMap<>();

    public void readFastq(String fileName) {
        try (
                InputStream fileStream = new FileInputStream(fileName);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(decoder)
        ) {
            String header = null;
            String sequence = null;
            String phred;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("+")) continue;

                if (line.startsWith("@") && header==null) {
                    header = line.substring(1);
                    continue;
                }
                if (header != null && sequence == null) {
                    sequence = line;
                    continue;
                }
                if (header != null) {
                    phred = line;
                    fastq.putIfAbsent(header, new Sequence(header, sequence, phred));
                    if (phred.length() != sequence.length()) {
                        System.out.println(header + "has unequal sequence lengths");
                    }
                    header = null;
                    sequence = null;
                }
            }

        } catch (Exception e) {
            System.out.println("Error opening file: " + fileName);
            throw new RuntimeException(e);
        }
        System.out.println("finished reading fastq file");
    }

    static void main() {
        FASTQ fastq = new FASTQ();
        long startTime = System.currentTimeMillis();
        System.out.println("starting to read fastq file");
        fastq.readFastq("/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R1_001.fastq.gz");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Time taken to read FASTQ file: " + duration + " milliseconds");
    }

}
