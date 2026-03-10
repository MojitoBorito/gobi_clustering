package com.filter;

import com.example.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AnchorPartition {

    HashMap<Long, CorrectedUMICluster> umiMap = new HashMap<>();
    private static final char[] bases = {'A', 'C', 'G', 'T'};
    HashSet<CorrectedUMICluster> canonicalClusters = new HashSet<>();
    int count = 0;
    static final int PHRED_THRESHOLD = 20;
    int clusterID = idCounter++;

    // Precomputed per-position multipliers for rolling hash
    private long[] positionMultipliers;
    private static final long HASH_BASE = 31L;
    private static int idCounter = 0;

    private void initMultipliers(int len) {
        if (positionMultipliers != null) return;
        positionMultipliers = new long[len];
        positionMultipliers[0] = 1;
        for (int i = 1; i < len; i++) {
            positionMultipliers[i] = positionMultipliers[i - 1] * HASH_BASE;
        }
    }

    private long hashChars(char[] chars) {
        long h = 0;
        for (int i = 0; i < chars.length; i++) {
            h += chars[i] * positionMultipliers[i];
        }
        return h;
    }

    int addRead(String umiSeq, byte[] umiPhred, String readSeq, byte[] readPhred, int umiWeight) {
        char[] umiChars = umiSeq.toCharArray();
        initMultipliers(umiChars.length);

        long baseHash = hashChars(umiChars);
        count++;

        // exact match first
        CorrectedUMICluster exact = umiMap.get(baseHash);
        if (exact != null) {
            exact.absorb(umiSeq, umiPhred, readSeq, readPhred, umiWeight);
            return exact.getClusterID();
        }

        // Collect low quality positions
        List<Integer> lowQualPositions = new ArrayList<>();
        for (int i = 0; i < umiPhred.length; i++) {
            int q = (umiPhred[i] & 0xFF) - 33;
            if (q < PHRED_THRESHOLD) {
                lowQualPositions.add(i);
            }
        }

        CorrectedUMICluster bestNeighbor = null;
        int bestNeighborCount = 0;
        int[] bestPositions = new int[2];
        int bestMutCount = 0;

        // 1-mismatch: adjust hash by swapping one position
        for (int idx : lowQualPositions) {
            char original = umiChars[idx];
            long hashWithout = baseHash - original * positionMultipliers[idx];

            for (char b : bases) {
                if (b == original) continue;
                long neighborHash = hashWithout + b * positionMultipliers[idx];
                CorrectedUMICluster candidate = umiMap.get(neighborHash);

                if (candidate != null && candidate.count > bestNeighborCount) {
                    bestNeighbor = candidate;
                    bestNeighborCount = candidate.count;
                    bestPositions[0] = idx;
                    bestMutCount = 1;
                }
            }
        }

        // 2-mismatch: adjust hash at two positions
        for (int a = 0; a < lowQualPositions.size(); a++) {
            int idx1 = lowQualPositions.get(a);
            char orig1 = umiChars[idx1];
            long hashWithout1 = baseHash - orig1 * positionMultipliers[idx1];

            for (char b1 : bases) {
                if (b1 == orig1) continue;
                long hash1 = hashWithout1 + b1 * positionMultipliers[idx1];

                for (int b = a + 1; b < lowQualPositions.size(); b++) {
                    int idx2 = lowQualPositions.get(b);
                    char orig2 = umiChars[idx2];
                    long hashWithout2 = hash1 - orig2 * positionMultipliers[idx2];

                    for (char b2 : bases) {
                        if (b2 == orig2) continue;
                        long neighborHash = hashWithout2 + b2 * positionMultipliers[idx2];
                        CorrectedUMICluster candidate = umiMap.get(neighborHash);

                        if (candidate != null && candidate.count > bestNeighborCount) {
                            bestNeighbor = candidate;
                            bestNeighborCount = candidate.count;
                            bestPositions[0] = idx1;
                            bestPositions[1] = idx2;
                            bestMutCount = 2;
                        }
                    }
                }
            }
        }

        if (bestNeighbor != null) {
            bestNeighbor.absorb(umiSeq, umiPhred, readSeq, readPhred, umiWeight);
            umiMap.put(baseHash, bestNeighbor);
            for (int i = 0; i < bestMutCount; i++) {
                Statistics.incrementUmiPos(bestPositions[i]);
                Statistics.addMutation((byte) umiChars[bestPositions[i]], bestNeighbor.umiConsensus[bestPositions[i]]);
            }
            return bestNeighbor.getClusterID();
        } else {
            CorrectedUMICluster newCluster = new CorrectedUMICluster(umiSeq, umiPhred, readSeq, readPhred, umiWeight);
            umiMap.put(baseHash, newCluster);
            canonicalClusters.add(newCluster);
            Statistics.incrementLargestAnchorCluster(canonicalClusters.size(), readSeq);
            return newCluster.getClusterID();
        }
    }

    public HashMap<Long, CorrectedUMICluster> getUmiMap() {
        return umiMap;
    }

    public int getCount() {
        return count;
    }

    public HashSet<CorrectedUMICluster> getCanonicalClusters() {
        return canonicalClusters;
    }

    public int getClusterID() {
        return clusterID;
    }
}