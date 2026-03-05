package com.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImprovedDualClustering {
    HashMap<String, AnchorPartition> partitions = new HashMap<>();

    public ImprovedDualClustering(UMI umis, String readFile) {
        improvedDualCluster(umis, readFile);
    }

    void improvedDualCluster(UMI umis, String readFile) {

        try (HashReadsIterator fastq = new HashReadsIterator(readFile)) {
            while (fastq.hasNext()) {
                HashReads read = fastq.next();

                // read.hash = last 50bp (your existing anchor)
                String anchor = read.hash;

                // Get UMI for this read
                UMICluster umiCluster = umis.header2Umis.get(read.header);

                // Get or create partition for this anchor
                AnchorPartition partition = partitions.computeIfAbsent(
                        anchor, _ -> new AnchorPartition()
                );

                // Add to partition with UMI correction
                partition.addRead(
                        umiCluster.seq,
                        umiCluster.phred,
                        read.seq,
                        read.phred
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, AnchorPartition> getPartitions() {
        return partitions;
    }

    public List<CorrectedUMICluster> getClusters(){
        List<CorrectedUMICluster> clusters = new ArrayList<>();
        for (AnchorPartition partition : partitions.values()) {
            clusters.addAll(partition.umiMap.values());
        }
        clusters.sort((x,y) -> Integer.compare(y.getCount(), x.getCount()));
        return clusters;
    }
}
