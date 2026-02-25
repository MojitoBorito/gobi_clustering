package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class UMI {

    HashMap<UMIseq, UMIseq> umis = new HashMap<>();
    HashMap<String, UMIseq> header2Umis = new HashMap<>();

    public UMI (String fileName){
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
            String phred;
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
                    phred = line;

                    addUMI(sequence, phred, header);

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

    public void addUMI(String sequence, String phred, String header){
        UMIseq temp = new UMIseq(sequence, false);
        UMIseq match = umis.get(temp);
        byte[] phredArray = phred.getBytes(StandardCharsets.US_ASCII);
        if (match != null){
            header2Umis.put(header, match);
            return;
        }
        byte[] curSeq = temp.seq;
        boolean put = false;
        for(UMIseq u : umis.keySet()){
            int dist = 0;
            for (int i = 0; i < curSeq.length; i++){
                if (curSeq[i] != u.consensus[i]){
                    dist++;
                }
                if (dist > 2) break;
            }
            if (dist <= 2){
                put = true;
                u.addSequence(curSeq, phredArray, header);
                header2Umis.put(header, u);
                break;
            }
        }
        if (!put){
            temp.makeStructures();
            temp.updateScore(curSeq, phredArray);
            header2Umis.put(header, temp);
            umis.put(temp, temp);
        }
    }

    public void filterUMI(){
        HashMap<Long, UMIseq> consensusMap = new HashMap<>();
        for (UMIseq u : umis.keySet()){
            long cHash = packSequence(u.consensus);
            consensusMap.putIfAbsent(cHash, u);
        }
        for (Map.Entry<String, UMIseq> entry : header2Umis.entrySet()){
            UMIseq u = entry.getValue();
            UMIseq existing = consensusMap.get(packSequence(u.consensus));
            if (existing != null){
                entry.setValue(existing);
            }
        }
    }

    public static long packSequence(byte[] seq) {
        long packed = 0;
        for (byte b : seq) {
            long bits = switch (b) {
                case 'A', 'a' -> 0L;
                case 'C', 'c' -> 1L;
                case 'G', 'g' -> 2L;
                case 'T', 't' -> 3L;
                default -> 4L;  // N
            };
            packed = (packed << 3) | bits;
        }
        return packed;
    }

}
