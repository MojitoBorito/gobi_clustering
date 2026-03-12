package com.cli;

import org.apache.commons.cli.*;

public class CliParser {

    public static CmdOptions parse(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption(
                Option.builder("umi")
                        .hasArg()
                        .argName("file")
                        .desc("Path to UMI file (required for UMI-based clustering)")
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
                        .argName("dir")
                        .desc("Output directory")
                        .required()
                        .get());

        options.addOption(
                Option.builder("kmer_size")
                        .hasArg()
                        .argName("int")
                        .desc("K-mer size (required for secondary clustering)")
                        .get());

        options.addOption(
                Option.builder("threshold")
                        .hasArg()
                        .argName("double")
                        .desc("Threshold (required for secondary clustering)")
                        .get());

        options.addOption(
                Option.builder("read_length")
                        .hasArg()
                        .argName("int")
                        .desc("Read length (required for secondary clustering)")
                        .get());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String umi = cmd.getOptionValue("umi");
        String reads = cmd.getOptionValue("reads");
        String outDir = cmd.getOptionValue("outDir");

        boolean runSecondaryClustering = (umi == null);

        Integer kmerSize = null;
        Double threshold = null;
        Integer readLength = null;

        if (runSecondaryClustering) {
            if (!cmd.hasOption("kmer_size") ||
                    !cmd.hasOption("threshold") ||
                    !cmd.hasOption("read_length")) {
                throw new ParseException(
                        "If --umi is not provided, --kmer_size, --threshold, and --read_length are required."
                );
            }

            try {
                kmerSize = Integer.parseInt(cmd.getOptionValue("kmer_size"));
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid value for --kmer_size: must be an integer.");
            }

            try {
                threshold = Double.parseDouble(cmd.getOptionValue("threshold"));
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid value for --threshold: must be a double.");
            }

            try {
                readLength = Integer.parseInt(cmd.getOptionValue("read_length"));
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid value for --read_length: must be an integer.");
            }
        }

        return new CmdOptions(
                umi,
                reads,
                outDir,
                runSecondaryClustering,
                kmerSize,
                threshold,
                readLength
        );
    }
}