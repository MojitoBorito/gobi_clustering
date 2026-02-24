package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
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

                if (header==null && line.startsWith("@")) {
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
        System.out.println("Reading fastq file...");
        fastq.readFastq("/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R2_001.fastq.gz");
        System.out.println("Counting UMIs...");
        HashMap<String, Integer> umis = new HashMap<>();
        for  (Sequence seq : fastq.fastq.values()) {
            int c = umis.getOrDefault(seq.sequence, 0);
            umis.put(seq.sequence, c+1);
        }
        System.out.println("Writing output file...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/mnt/biocluster/praktikum/genprakt/patil/Blockteil/umi_counts.tsv"))) {
            writer.write("umi\tcount\n");
            for (String key : umis.keySet()) {
                writer.write(String.format("%s\t%d\n", key, umis.get(key)));
            }
        }catch (Exception e){
            System.out.println("Error writing file");
            throw new RuntimeException(e);
        }
    }

}
