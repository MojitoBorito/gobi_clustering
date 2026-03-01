package com.filter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

public class DualClustering {
    HashMap<String, SubCluster> header2Seq = new HashMap<>();
    HashMap<String, SubCluster> header2Umi = new HashMap<>();

    HashSet<SubCluster> umiClusters = new HashSet<>();
    HashSet<SubCluster> seqClusters = new HashSet<>();

    Reads readClustering = new Reads();

    public DualClustering(UMI umis, String readFile){
        dualCluster(umis, readFile);
    }

    public void dualCluster(UMI umis, String readFile){
        try (HashReadsIterator fastq = new HashReadsIterator(readFile)){
            while (fastq.hasNext()) {
                HashReads read = fastq.next();
                ReadCluster readCluster = readClustering.clusters.computeIfAbsent(read, _ -> new ReadCluster());
                readCluster.n++;

                String header = read.header;
                UMICluster umiCluster = umis.header2Umis.get(header);
                int[] umiPhred = umiCluster.phred;

                //start by clustering the sequences within the UMIs
                if (umiCluster.sub50cluster == null){
                    umiCluster.sub50cluster = new HashMap<>();
                }
                SubCluster cluster = umiCluster.sub50cluster.computeIfAbsent(read, r -> new SubCluster(r.phred.length));
                cluster.updateScore(read.phred, read.source, readCluster.n);
                cluster.n++;
                header2Seq.put(header, cluster);
                seqClusters.add(cluster);

                //cluster the UMIs for sequence correction with the clustered 50bp sequences.
                readCluster.correctUmi(umiCluster.seq, umiPhred, umiCluster.n);
                header2Umi.put(header, readCluster.umis);
                umiClusters.add(readCluster.umis);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetDualClustering(UMI umis){
        for (String header : umis.header2Umis.keySet()){
            umis.umis.get(header).sub50cluster = null;
        }
        SubCluster.resetIdCreator();
    }

    public HashMap<String, SubCluster> getHeader2Seq() {
        return header2Seq;
    }

    public HashMap<String, SubCluster> getHeader2Umi() {
        return header2Umi;
    }

    public HashSet<SubCluster> getSeqClusters() {
        return seqClusters;
    }

    public HashSet<SubCluster> getUmiClusters() {
        return umiClusters;
    }

}
