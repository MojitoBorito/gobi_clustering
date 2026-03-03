package com.filter;

import java.io.IOException;

public class Cluster50BP {

    Reads readClustering = new Reads();

    public void cluster(String readFile){
        try(HashReadsIterator fastq = new HashReadsIterator(readFile)){
            while(fastq.hasNext()){
                HashReads read = fastq.next();
                ReadCluster readCluster = readClustering.clusters.computeIfAbsent(read.hash, _ -> new ReadCluster());
                readCluster.n++;
                readCluster.correctSequence(read.seq, read.phred);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
