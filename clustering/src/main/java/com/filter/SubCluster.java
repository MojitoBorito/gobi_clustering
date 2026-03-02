package com.filter;

public class SubCluster {
    private static int idCreator = 0;

    final int id;
    int[] bestScore;
    byte[] consensus;
    int n;
    int k;

    public SubCluster(int length) {
        bestScore = new int[length];
        consensus = new byte[length];
        n = 0;
        k=0;
        id = idCreator++;
    }

    public void updateScore(int[] phred, String seq) {
        for (int i = 0; i < seq.length(); i++) {
            byte base = (byte) seq.charAt(i);
            if (consensus[i] == 0 || consensus[i] == base) {
                consensus[i] = base;
                bestScore[i] = (bestScore[i] * k + phred[i]) / (k+1);
                k++;
            } else if (phred[i] > bestScore[i]) {
                consensus[i] = base;
                bestScore[i] = phred[i];
                k=0;
            }
        }
        n++;
    }

    public void updateScore(byte[] phred, String seq) {
        for (int i = 0; i < seq.length(); i++) {
            byte base = (byte) seq.charAt(i);
            if (consensus[i] == 0 || consensus[i] == base) {
                consensus[i] = base;
                bestScore[i] = (bestScore[i] * k + phred[i]) / (k+1);
                k++;
            } else if (phred[i] > bestScore[i]) {
                consensus[i] = base;
                bestScore[i] = phred[i];
                k=0;
            }
        }
        n++;
    }


    public void updateSequence() {
        for (int i = 0; i < consensus.length; i++) {
            if (consensus[i] == 0) {
                consensus[i] = 'N';
            }
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof SubCluster other) {
            return this.id == other.id;
        }
        return false;
    }

    public static void resetIdCreator() {
        idCreator = 0;
    }

    public byte[] getConsensus() {
        return consensus;
    }

    public int getN() {
        return n;
    }
}