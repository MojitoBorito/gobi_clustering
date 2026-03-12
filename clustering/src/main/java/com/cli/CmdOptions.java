package com.cli;

public record CmdOptions(String umi,
                         String reads,
                         String outDir,
                         boolean secondCycle,
                         Integer kmerSize,
                         Double threshold,
                         Integer readLength) {

}
