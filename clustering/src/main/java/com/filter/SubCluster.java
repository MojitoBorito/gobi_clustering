package com.filter;

public class SubCluster {
    private static int idCreator = 0;

    final int id;
    int[][] score;
    byte[] consensus;
    int n;

    private static final byte[] INDEX_TO_BASE = {'A', 'C', 'G', 'T', 'N'};

    public SubCluster(int length) {
        score = new int[5][length];
        consensus = new byte[length];
        n=0;
        id = idCreator++;
    }

    public void updateScore(int[] phred, byte[] seq, int m){
        int index;
        for (int i = 0; i < seq.length; i++){
            index = baseToIndex(seq[i]);
            score[index][i] = (score[index][i] * n + phred[i] * m) / (n+m);
        }
        n+=m;
    }

    public void updateScore(byte[] phred, byte[] seq, int m){
        int index;
        for (int i = 0; i < seq.length; i++){
            index = baseToIndex(seq[i]);
            score[index][i] = (score[index][i] * n + phred[i] * m) / (n+m);
        }
        n+=m;
    }

    private static int baseToIndex(byte base) {
        switch (base) {
            case 'A':
            case 'a':
                return 0;
            case 'C':
            case 'c':
                return 1;
            case 'G':
            case 'g':
                return 2;
            case 'T':
            case 't':
                return 3;
            default:
                return 4;
        }
    }

    public void updateSequence(){
        int bestIndex;
        int bestScore;

        for (int i = 0; i < consensus.length; i++){
            bestIndex = 0;
            bestScore = score[0][i];
            for (int j = 1; j < 4; j++){
                if (score[j][i] > bestScore){
                    bestIndex = j;
                    bestScore = score[j][i];
                }
            }
            if (bestScore > 0){
                consensus[i] = INDEX_TO_BASE[bestIndex];
            } else {
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
