package com.filter;

import com.example.Statistics;
import com.example.Validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AnchorPartition {
    HashMap<String, CorrectedUMICluster> umiMap = new HashMap<>();
    private static final char[] bases = {'A', 'C', 'G', 'T'};
    HashSet<CorrectedUMICluster> canonicalClusters = new HashSet<>();
    int count = 0;
    //static final int PHRED_THRESHOLD = 30; // 0.01% error rate -> stricter
    static final int PHRED_THRESHOLD = 20; // 0.1% error rate -> more lax

    void addRead(String umiSeq, byte[] umiPhred, String readSeq, byte[] readPhred) {

        // exact match first
        CorrectedUMICluster exact = umiMap.get(umiSeq);
        if (exact != null) {
            exact.absorb(umiSeq, umiPhred, readSeq, readPhred);
            return;
        }

        // Collect low quality positions
        List<Integer> lowQualPositions = new ArrayList<>();
//        HashSet<String> possiblities = new HashSet<>();
        boolean neighbourFound = false;

        for (int i = 0; i < umiPhred.length; i++) {
            int q = (umiPhred[i] & 0xFF) - 33;
            if (q < PHRED_THRESHOLD) {
                lowQualPositions.add(i);
            }
        }

        // Allow up to 2 errors -> first single then double
        CorrectedUMICluster bestNeighbor = null;
        int bestNeighborCount = 0;
        int[] bestPositions = new int[2];
        int bestMutCount = 0;

        char[] umiChars = umiSeq.toCharArray();

        // 1-mismatch neighbors
        for (int idx : lowQualPositions) {
            char original = umiChars[idx];
            for (char b : bases) {
                if (b == original) continue;
                umiChars[idx] = b;
                String neighbor = new String(umiChars);
                CorrectedUMICluster candidate = umiMap.get(neighbor);

                if (candidate != null && candidate.count > bestNeighborCount) {
                    bestNeighbor = candidate;
                    bestNeighborCount = candidate.count;
                    bestPositions[0] = idx;
                    bestMutCount = 1;
                    neighbourFound = true;
                }
//                if (!neighbourFound){
//                    possiblities.add(neighbor);
//                }
                umiChars[idx] = original;
            }
        }

        // 2-mismatch neighbors
        for (int a = 0; a < lowQualPositions.size(); a++) {
            int idx1 = lowQualPositions.get(a);
            char orig1 = umiChars[idx1];
            for (char b1 : bases) {
                if (b1 == orig1) continue;
                umiChars[idx1] = b1;

                for (int b = a + 1; b < lowQualPositions.size(); b++) {
                    int idx2 = lowQualPositions.get(b);
                    char orig2 = umiChars[idx2];
                    for (char b2 : bases) {
                        if (b2 == orig2) continue;
                        umiChars[idx2] = b2;
                        String neighbor = new String(umiChars);
                        CorrectedUMICluster candidate = umiMap.get(neighbor);

                        if (candidate != null && candidate.count > bestNeighborCount) {
                            bestNeighbor = candidate;
                            bestNeighborCount = candidate.count;
                            bestPositions[0] = idx1;
                            bestPositions[1] = idx2;
                            bestMutCount = 2;
                            neighbourFound = true;
                        }
//                        if (!neighbourFound){
//                            possiblities.add(neighbor);
//                        }
                        umiChars[idx2] = orig2;
                    }
                }
                umiChars[idx1] = orig1;
            }
        }

        // Merge or create new cluster
        if (bestNeighbor != null) {
            bestNeighbor.absorb(umiSeq, umiPhred, readSeq, readPhred);
            umiMap.put(umiSeq, bestNeighbor);
            for (int i = 0; i < bestMutCount; i++) {
                Statistics.incrementUmiPos(bestPositions[i]);
            }
        } else {
            CorrectedUMICluster newCluster = new CorrectedUMICluster(umiSeq, umiPhred, readSeq, readPhred);
            umiMap.put(umiSeq, newCluster);
            canonicalClusters.add(newCluster);
//            addErroneousVariants(possiblities, newCluster);
        }
        count++;
        Statistics.incrementLargestAnchorCluster(canonicalClusters.size(), readSeq);
    }

    private void addErroneousVariants(HashSet<String> possibilities, CorrectedUMICluster cluster){
        for (String umi : possibilities) {
            umiMap.put(umi, cluster);
        }
    }

    public HashMap<String, CorrectedUMICluster> getUmiMap() {
        return umiMap;
    }
}
