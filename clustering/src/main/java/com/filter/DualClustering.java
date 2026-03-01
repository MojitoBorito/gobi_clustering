package com.filter;

import java.util.HashMap;

public class DualClustering {
    HashMap<String, SubCluster> header2Seq = new HashMap<>();
    HashMap<String, SubCluster> header2Umi = new HashMap<>();

    public DualClustering(UMI umis, Reads reads){
        dualCluster(umis, reads);
    }

    public void dualCluster(UMI umis, Reads reads){
        for (String header : reads.sequences.keySet()){
            HashReads read = reads.sequences.get(header);
            UMICluster umiCluster = umis.header2Umis.get(header);
            ReadCluster readCluster = reads.clusters.get(read);
            int[] umiPhred = umiCluster.phred;

            //start by clustering the sequences within the UMIs
            if (umiCluster.sub50cluster == null){
                umiCluster.sub50cluster = new HashMap<>();
            }
            SubCluster cluster = umiCluster.sub50cluster.computeIfAbsent(read, r -> new SubCluster(r.phred.length));
            cluster.updateScore(read.phred, read.source, readCluster.n);
            cluster.n++;
            header2Seq.put(header, cluster);

            //cluster the UMIs for sequence correction with the clustered 50bp sequences.
            readCluster.correctUmi(umiCluster.seq, umiPhred, umiCluster.n);
            header2Umi.put(header, readCluster.umis);
        }
    }

    public static void resetDualClustering(UMI umis){
        for (String header : umis.header2Umis.keySet()){
            umis.umis.get(header).sub50cluster = null;
        }
    }


}
