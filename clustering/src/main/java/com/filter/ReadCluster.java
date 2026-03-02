package com.filter;

public class ReadCluster{
    int n = 0;

    SubCluster umis = null;

    public void correctUmi(byte[] seq, int[] phred){
        if (umis == null) {
            umis = new SubCluster(phred.length);
        }
        umis.updateScore(phred, seq);
    }

    public byte[] getCorrectedUmi(){
        umis.updateSequence();
        return umis.consensus;
    }

    public SubCluster getUmis() {
        return umis;
    }
}
