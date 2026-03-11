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
                Option.builder("outDir")
                        .hasArg()
                        .argName("file")
                        .desc("Output cluster file")
                        .required()
                        .get());


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String umi = cmd.getOptionValue("umi");
        String reads = cmd.getOptionValue("reads");
        String outDir = cmd.getOptionValue("outDir");

        return new CmdOptions(umi, reads, outDir);
    }
}
