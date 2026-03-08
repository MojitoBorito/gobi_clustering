package com.cli;

public class CmdOptions {

    private final String umi;
    private final String reads;
    private final String outCluster;
    private final String outUmi;
    private final String outAnchor;
    private final String outPos;
    private final String outMut;


    public CmdOptions(String umi, String reads, String outCluster,
                      String outUmi, String outAnchor, String outPos, String outMut) {
        this.umi = umi;
        this.reads = reads;
        this.outCluster = outCluster;
        this.outUmi = outUmi;
        this.outAnchor = outAnchor;
        this.outPos = outPos;
        this.outMut = outMut;
    }

    public String getUmi() {
        return umi;
    }

    public String getReads() {
        return reads;
    }

    public String getOutCluster() {
        return outCluster;
    }

    public String getOutUmi() {
        return outUmi;
    }

    public String getOutAnchor() {
        return outAnchor;
    }

    public String getOutPos() {
        return outPos;
    }

    public String getOutMut() {
        return outMut;
    }
}
