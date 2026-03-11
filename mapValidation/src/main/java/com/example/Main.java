package com.example;

public class Main {
    static void main(String[] args) {
        CmdParser parser = new CmdParser("-predicted", "-bam", "-out", "-outBAM");
        parser.setFile("-predicted", "-bam", "-out", "-outBAM");

        parser.parse(args);

        String pred = parser.getValue("-predicted");
        String bam = parser.getValue("-bam");
        String out = parser.getValue("-out");
        String outBAM = parser.getValue("-outBAM");

        Analyser analyser = new Analyser(pred, bam);
        analyser.validateClusterLevel(out);
        analyser.writeBamClusterSizes(outBAM);
    }
}
