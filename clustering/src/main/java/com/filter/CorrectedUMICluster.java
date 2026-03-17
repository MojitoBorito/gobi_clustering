package com.filter;

import com.example.Statistics;
import com.model.Element;
import com.model.UmiRead;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class CorrectedUMICluster {
    int count = 0;

    private static int idCounter = 0;

    // Phred-weighted consensus for the UMI itself
    short[] umiBestScore;
    byte[]  umiConsensus;

    // Phred-weighted consensus for the read
    short[] readBestScore;
    byte[]  readConsensus;

    int clusterID;

    CorrectedUMICluster(String umiSeq, byte[] umiPhred, String readSeq, byte[] readPhred) {
        int umiLen = umiSeq.length();
        int readLen = readSeq.length();

        umiBestScore  = new short[umiLen];
        umiConsensus  = new byte[umiLen];
        readBestScore = new short[readLen];
        readConsensus = new byte[readLen];

        clusterID = idCounter++;

        absorb(umiSeq, umiPhred, readSeq, readPhred);
    }

    //Merge a new observation into this cluster.

    void absorb(String umiSeq, byte[] umiPhred, String readSeq, byte[] readPhred) {
        updateConsensus(umiConsensus, umiBestScore, umiSeq, umiPhred);
        updateConsensus(readConsensus, readBestScore, readSeq, readPhred);
        count++;
        Statistics.incrementLargestUmiAnchorCluster(count, umiConsensus, readConsensus);
    }

    private void updateConsensus(byte[] consensus, short[] bestScore, String seq, byte[] phred) {
        try {
            for (int i = 0; i < seq.length(); i++) {
                byte base = (byte) seq.charAt(i);
                int q = phred[i] & 0xFF;
                if (consensus[i] == 0 || consensus[i] == base) {
                    consensus[i] = base;
                    bestScore[i] = (short) Math.min(bestScore[i] + q, Short.MAX_VALUE);
                } else if (q > bestScore[i]) {
                    consensus[i] = base;
                    bestScore[i] = (short) q;
                }
            }
        }catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println(new String(consensus, StandardCharsets.UTF_8));
            System.out.println(Arrays.toString(bestScore));
            System.out.println(Arrays.toString(phred));
            System.out.println(seq);
            throw new RuntimeException(e);
        }
    }

    public String getUmi(){
        return new String(umiConsensus, StandardCharsets.US_ASCII);
    }

    public String getRead(){
        return new String(readConsensus, StandardCharsets.US_ASCII);
    }

    public int getCount() {
        return count;
    }

    public int getClusterID() {
        return clusterID;
    }
}
