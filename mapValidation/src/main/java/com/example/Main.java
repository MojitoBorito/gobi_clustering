package com.example;

public class Main {
    static void main(String[] args) {
        CmdParser parser = new CmdParser("-predicted", "-bam", "-out");
        parser.setFile("-predicted", "-bam", "-out");

        parser.parse(args);

        String pred = parser.getValue("-predicted");
        String bam = parser.getValue("-bam");
        String out = parser.getValue("-out");

        Analyser analyser = new Analyser(pred, bam);
        analyser.validate(out);
    }
}
