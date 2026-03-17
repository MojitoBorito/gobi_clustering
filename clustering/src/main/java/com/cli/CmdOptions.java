package com.cli;

public record CmdOptions(String umi,
                         String reads,
                         String outDir,
                         boolean runPrimaryClustering,
                         boolean runSecondaryClustering,
                         Integer kmerSize,
                         Double threshold,
                         Integer readLength,
                         Integer umiLength) {

}
