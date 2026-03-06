package com.filter;

import com.example.Statistics;
import com.example.Validation;

import java.util.HashMap;
import java.util.HashSet;

public class AnchorPartition {
    HashMap<String, CorrectedUMICluster> umiMap = new HashMap<>();
    private static final char[] bases = {'A', 'C', 'G', 'T'};
    HashSet<CorrectedUMICluster> canonicalClusters = new HashSet<>();
    int count = 0;

    void addRead(String umiSeq, byte[] umiPhred, String readSeq, byte[] readPhred) {

        //exact match first
        CorrectedUMICluster exact = umiMap.get(umiSeq);
        if (exact != null) {
            exact.absorb(umiSeq, umiPhred, readSeq, readPhred);
            return;
        }

        //Enumerate all 1-Hamming-distance neighbors.
        CorrectedUMICluster bestNeighbor = null;
        int bestNeighborCount = 0;
        int position = 0;

        char[] umiChars = umiSeq.toCharArray();

        for (int i = 0; i < umiChars.length; i++) {
            char original = umiChars[i];
            for (char b : bases) {
                if (b == original) continue;
                umiChars[i] = b;
                String neighbor = new String(umiChars);
                CorrectedUMICluster candidate = umiMap.get(neighbor);

                if (candidate != null && candidate.count > bestNeighborCount) {
                    // Directional: only merge INTO larger clusters. erroneous copies are minorities
                    bestNeighbor = candidate;
                    bestNeighborCount = candidate.count;
                    position = i;
                }
                umiChars[i] = original;
            }
        }

        // only merge if the differing position has a LOW Phred score (= the base is likely an error)
        if (bestNeighbor != null) {
            bestNeighbor.absorb(umiSeq, umiPhred, readSeq, readPhred);
            //put UMI in neighbour cluster so that erroneous looks are fast
            umiMap.put(umiSeq, bestNeighbor);
            Statistics.incrementUmiPos(position);
        } else {
            //No match at all -> new original molecule
            CorrectedUMICluster newCluster = new CorrectedUMICluster(umiSeq, umiPhred, readSeq, readPhred);
            umiMap.put(umiSeq, newCluster);
            canonicalClusters.add(newCluster);
        }
        count++;
        Statistics.incrementLargestAnchorCluster(count, readSeq);
    }

    public HashMap<String, CorrectedUMICluster> getUmiMap() {
        return umiMap;
    }
}
