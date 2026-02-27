package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class BaseCluster {

    HashMap<String, BaseClusterSeq> umis = new HashMap<>();
    HashMap<String, BaseClusterSeq> header2Umis = new HashMap<>();

    public BaseCluster(String fileName){
        readFastq(fileName);
    }

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
                    addUMI(sequence, header);

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

    public void addUMI(String sequence, String header){
        BaseClusterSeq match = umis.get(sequence);
        if (match != null){
            header2Umis.put(header, match);
            match.n++;
            return;
        }
        BaseClusterSeq umi = new BaseClusterSeq(sequence.getBytes(StandardCharsets.US_ASCII));
        header2Umis.put(header, umi);
        umis.put(sequence, umi);
    }

}
