package com.example;

import java.io.*;
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
    }

    static void main() {
        FASTQ fastq = new FASTQ();
        fastq.readFastq("/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R2_001.fastq.gz");
        HashMap<String, Integer> umis = new HashMap<>();
        for  (Sequence seq : fastq.fastq.values()) {
            int c = umis.getOrDefault(seq.sequence, 0);
            umis.put(seq.sequence, c+1);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/mnt/biocluster/praktikum/genprakt/patil/Blockteil"))){
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
