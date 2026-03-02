package com.filter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class DualClustering {
//    HashMap<String, SubCluster> header2Seq = new HashMap<>();
//    HashMap<String, SubCluster> header2Umi = new HashMap<>();

//    HashSet<SubCluster> umiClusters = new HashSet<>();
//    HashSet<SubCluster> seqClusters = new HashSet<>();

    Reads readClustering = new Reads();
    UMI umiClustering;

    public DualClustering(UMI umis, String readFile){
        dualCluster(umis, readFile);
        this.umiClustering = umis;
    }

    public void dualCluster(UMI umis, String readFile){
        try (HashReadsIterator fastq = new HashReadsIterator(readFile)){
            while (fastq.hasNext()) {
                HashReads read = fastq.next();
                ReadCluster readCluster = readClustering.clusters.computeIfAbsent(read.hash, _ -> new ReadCluster());
                readCluster.n++;

                String header = read.header;
                UMICluster umiCluster = umis.header2Umis.get(header);
                int[] umiPhred = umiCluster.phred;

                //start by clustering the sequences within the UMIs
                SubCluster cluster = umis.subClusters.computeIfAbsent(read.hash + umiCluster.seq,
                        _ -> new SubCluster(read.phred.length));
                cluster.updateScore(read.phred, read.seq);
//                header2Seq.put(header, cluster);

                //cluster the UMIs for sequence correction with the clustered 50bp sequences.
                readCluster.correctUmi(umiCluster.seq, umiPhred);
//                header2Umi.put(header, readCluster.umis);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetDualClustering(UMI umis){
        umis.subClusters = new HashMap<>();
        SubCluster.resetIdCreator();
    }

//    public HashMap<String, SubCluster> getHeader2Seq() {
//        return header2Seq;
//    }
//
//    public HashMap<String, SubCluster> getHeader2Umi() {
//        return header2Umi;
//    }


    public Reads getReadClustering() {
        return readClustering;
    }

    public UMI getUmiClustering() {
        return umiClustering;
    }

    public static void main(String[] args) throws IOException {
        String umi = "/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R2_001.fastq.gz";
        String fw = "/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R1_001.fastq.gz";

        String umiOut = "/mnt/biocluster/praktikum/genprakt/patil/Blockteil/dual_out/umi.txt";
        String fwOut = "/mnt/biocluster/praktikum/genprakt/patil/Blockteil/dual_out/fw.txt";

        long starTime = System.currentTimeMillis();

        UMI umiGroup = new UMI(umi);

        long endTime = System.currentTimeMillis();
        long first = endTime - starTime;

        starTime = System.currentTimeMillis();

        DualClustering dualClustering = new DualClustering(umiGroup, fw);
        
        endTime = System.currentTimeMillis();
        long second = endTime - starTime;


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(umiOut))){
            writer.write("seq\tcounts\n");
            for (ReadCluster reads : dualClustering.readClustering.clusters.values()){
                SubCluster seq = reads.umis;
                seq.updateSequence();
                writer.write(new String(seq.consensus, StandardCharsets.US_ASCII) + "\t" + seq.n + "\n");
            }
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fwOut))){
            writer.write("seq\tcounts\n");
            for (SubCluster seq : dualClustering.umiClustering.subClusters.values()){
                seq.updateSequence();
                writer.write(new String(seq.consensus, StandardCharsets.US_ASCII) + "\t" + seq.n + "\n");
            }
        }
        
        System.out.println("Umi cluster time: "+(first/1000));
        System.out.println("Dual cluster time: "+(second/1000));
    }

}
