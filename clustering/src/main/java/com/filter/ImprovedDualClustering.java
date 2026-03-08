package com.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImprovedDualClustering {
    HashMap<String, AnchorPartition> clusters = new HashMap<>();

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
                AnchorPartition cluster = this.clusters.computeIfAbsent(
                        anchor, _ -> new AnchorPartition()
                );

                // Add to partition with UMI correction
                cluster.addRead(
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
        return clusters;
    }

    public List<CorrectedUMICluster> getClusters(){
        List<CorrectedUMICluster> clusterList = new ArrayList<>();
        for (AnchorPartition partition : clusters.values()) {
            clusterList.addAll(partition.canonicalClusters);
        }
        clusterList.sort((x,y) -> Integer.compare(y.getCount(), x.getCount()));
        return clusterList;
    }

    public HashMap<String, Integer> getCorrectedUmis(){
        HashMap<String, Integer> correctedUmis = new HashMap<>();
        for (AnchorPartition partition : clusters.values()) {
            for (CorrectedUMICluster cluster: partition.canonicalClusters) {
                String umi = cluster.getUmi();
                correctedUmis.put(umi,
                        correctedUmis.getOrDefault(umi, 0) + cluster.getCount());
            }
        }
        return correctedUmis;
    }

}
