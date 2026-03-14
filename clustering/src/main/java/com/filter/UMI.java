package com.filter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class UMI {

    HashMap<String, UMICluster> umis = new HashMap<>();
    HashMap<String, UMICluster> header2Umis = new HashMap<>();
    int numUmis = 0;

    public UMI(String fileName){
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
                    int spaceIdx = line.indexOf(' ');
                    header = (spaceIdx == -1)
                            ? line.substring(1)
                            : line.substring(1, spaceIdx);
                    continue;
                }
                if (header != null && sequence == null) {
                    sequence = line;
                    continue;
                }
                if (header != null) {
                    addUMI(sequence, header, line);
                    header = null;
                    sequence = null;
                }
            }

        } catch (Exception e) {
            System.out.println("Error opening file: " + fileName);
            throw new RuntimeException(e);
        }
        numUmis = umis.size();
        umis = null;
        System.gc();
    }

    public void addUMI(String sequence, String header, String phred){
        UMICluster match = umis.get(sequence);
        if (match != null){
            header2Umis.put(header, match);
            match.updatePhred(phred.getBytes(StandardCharsets.US_ASCII));
            return;
        }
        UMICluster umi = new UMICluster(sequence, phred.getBytes(StandardCharsets.US_ASCII));
        header2Umis.put(header, umi);
        umis.put(sequence, umi);
    }

    public HashMap<String, UMICluster> getHeader2Umis() {
        return header2Umis;
    }

    public HashMap<String, UMICluster> getUmis() {
        return umis;
    }

    public int getNumUmis() {
        return numUmis;
    }
}
