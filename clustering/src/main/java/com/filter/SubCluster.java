package com.filter;

public class SubCluster {
    private static int idCreator = 0;

    final int id;
    short[] bestScore;
    byte[] consensus;
    int n;

    public SubCluster(int length) {
        bestScore = new short[length];
        consensus = new byte[length];
        n = 0;
        id = idCreator++;
    }

    public void updateScore(int[] phred, String seq) {
        for (int i = 0; i < seq.length(); i++) {
            byte base = (byte) seq.charAt(i);
            if (consensus[i] == 0 || consensus[i] == base) {
                consensus[i] = base;
                bestScore[i] = (short) Math.min(bestScore[i] + phred[i], Short.MAX_VALUE);
            } else if (phred[i] > bestScore[i]) {
                consensus[i] = base;
                bestScore[i] = (short) phred[i];
            }
        }
        n++;
    }

    public void updateScore(byte[] phred, String seq) {
        for (int i = 0; i < seq.length(); i++) {
            byte base = (byte) seq.charAt(i);
            if (consensus[i] == 0 || consensus[i] == base) {
                consensus[i] = base;
                bestScore[i] = (short) Math.min(bestScore[i] + (phred[i] & 0xFF), Short.MAX_VALUE);
            } else if ((phred[i] & 0xFF) > bestScore[i]) {
                consensus[i] = base;
                bestScore[i] = (short) (phred[i] & 0xFF);
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
    public int hashCode() { return id; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof SubCluster other) {
            return this.id == other.id;
        }
        return false;
    }

    public static void resetIdCreator() { idCreator = 0; }

    public byte[] getConsensus() { return consensus; }

    public int getN() { return n; }
}