package com.cli;

public class CmdOptions {

    private final String umi;
    private final String reads;
    private final String outDir;


    public CmdOptions(String umi, String reads, String outDir) {
        this.umi = umi;
        this.reads = reads;
        this.outDir = outDir;
    }

    public String getUmi() {
        return umi;
    }

    public String getReads() {
        return reads;
    }

    public String getOutDir() {
        return outDir;
    }
}
