package com.filter;

import com.example.Sequence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class Reads {

    HashMap<HashReads, ReadCluster> clusters = new HashMap<>();

    public void readFastq(String fileName) {
        try (
                InputStream fileStream = new FileInputStream(fileName);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(decoder)
        ) {
            String header = null;
            String sequence = null;
            String line;
            String phred;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("+")) continue;

                if (header==null && line.startsWith("@")) {
                    header = line.substring(1).split(" ")[0];
                    continue;
                }
                if (header != null && sequence == null) {
                    sequence = line;
                    continue;
                }
                if (header != null) {
                    phred = line;

                    HashReads read = new HashReads(
                            header,
                            sequence.getBytes(StandardCharsets.US_ASCII),
                            phred.getBytes(StandardCharsets.US_ASCII)
                    );
                    ReadCluster readCluster = clusters.computeIfAbsent(read, _ -> new ReadCluster());
                    readCluster.n++;

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
}
