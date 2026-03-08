package com.cli;

import org.apache.commons.cli.*;


public class CliParser {

    public static CmdOptions parse(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(
                Option.builder("umi")
                        .hasArg()
                        .argName("file")
                        .desc("Path to UMI file")
                        .required()
                        .get());

        options.addOption(
                Option.builder("reads")
                        .hasArg()
                        .argName("file")
                        .desc("Path to reads file")
                        .required()
                        .get());

        options.addOption(
                Option.builder("outCluster")
                        .hasArg()
                        .argName("file")
                        .desc("Output cluster file")
                        .required()
                        .get());

        options.addOption(
                Option.builder("outUmi")
                        .hasArg()
                        .argName("file")
                        .desc("Corrected Umi Counts")
                        .required()
                        .get());

        options.addOption(
                Option.builder("outAnchor")
                        .hasArg()
                        .argName("file")
                        .desc("Umi per Anchor counts")
                        .required()
                        .get());

        options.addOption(
                Option.builder("outPos")
                        .hasArg()
                        .argName("file")
                        .desc("Number of times a position was Mutated")
                        .required()
                        .get());

        options.addOption(
                Option.builder("outMut")
                        .hasArg()
                        .argName("file")
                        .desc("Number of times a Base Mutation happened")
                        .required()
                        .get());


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String umi = cmd.getOptionValue("umi");
        String reads = cmd.getOptionValue("reads");
        String outCluster = cmd.getOptionValue("outCluster");
        String outUmi = cmd.getOptionValue("outUmi");
        String outAnchor = cmd.getOptionValue("outAnchor");
        String outPos = cmd.getOptionValue("outPos");
        String outMut = cmd.getOptionValue("outMut");

        return new CmdOptions(umi, reads, outCluster, outUmi, outAnchor, outPos, outMut);
    }
}
