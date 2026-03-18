package ReadSimulation;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "ReadSimulator",
        mixinStandardHelpOptions = true,
        version = "ReadSimulator 1.0",
        description = "Simulates reads from transcripts."
)
public class Runner implements Callable<Integer> {

    @Option(names = "-length", required = true, description = "Read length")
    private int readLength;

    @Option(names = "-frlength", required = true, description = "Fragment length mean")
    private int frLength;

    @Option(names = "-SD", required = true, description = "Fragment length standard deviation")
    private int sd;

    @Option(names = "-readcounts", required = true, description = "Path to read counts file")
    private Path readCounts;

    @Option(names = "-mutationrate", required = true, description = "Mutation rate in percent (0.0-100.0)")
    private double mutationRatePercent;

    @Option(names = "-fasta", required = true, description = "Path to genome FASTA")
    private Path fasta;

    @Option(names = "-fidx", required = true, description = "Path to FASTA index (.fai)")
    private Path fidx;

    @Option(names = "-gtf", required = true, description = "Path to GTF annotation")
    private Path gtf;

    @Option(names = "-od", required = true, description = "Output directory")
    private Path od;

    @Option(names = "-pcrEfficiency", description = "PCR efficiency as probability in [0,1]")
    private Double pcrEfficiency;

    @Option(names = "-pcrCycles", description = "Number of PCR cycles")
    private Integer pcrCycles;

    @Option(names = "-flowCellCap", description = "Approximate flow cell capacity")
    private Integer capacity;

    @Option(names = "-umiLength", description = "UMI length")
    private Integer umiLength;

    @Option(names = "-numOfUmis", description = "total number of UMI sequences fragments will be assigned to")
    private Integer numOfUmis;



    public static void main(String[] args) {
        int exitCode = new CommandLine(new Runner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        validate();

        Files.createDirectories(od);

        ReadSimulator.simulateReads(
                gtf.toString(),
                fasta.toString(),
                fidx.toString(),
                readCounts.toString(),
                od.toString(),
                readLength,
                frLength,
                sd,
                mutationRatePercent,
                pcrEfficiency,
                capacity,
                pcrCycles,
                umiLength,
                numOfUmis
        );

        return 0;
    }

    private void validate() {
        if (readLength <= 0) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "-length must be > 0"
            );
        }

        if (frLength <= 0) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "-frlength must be > 0"
            );
        }

        if (sd < 0) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "-SD must be >= 0"
            );
        }

        if (mutationRatePercent < 0.0 || mutationRatePercent > 100.0) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "-mutationrate must be between 0.0 and 100.0"
            );
        }

        boolean allEqual = (pcrCycles == null) == (pcrEfficiency == null)
                && (pcrEfficiency == null) == (capacity == null)
                && (capacity == null) == (umiLength == null);
        if (!allEqual) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "If PCR amplification is enabled, all -pcrEfficiency, -pcrCycles, -flowCellCap and -umiLength must be provided."
            );
        }

        if (pcrEfficiency != null) {
            if (pcrEfficiency < 0.0 || pcrEfficiency > 100.0) {
                throw new CommandLine.ParameterException(
                        new CommandLine(this),
                        "-pcrEfficiency must be between 0.0 and 100.0"
                );
            }

            if (pcrCycles <= 0) {
                throw new CommandLine.ParameterException(
                        new CommandLine(this),
                        "-pcrCycles must be > 0"
                );
            }

            if (capacity < 0) {
                throw new CommandLine.ParameterException(
                        new CommandLine(this),
                        "-flowCellCap must be > 0"
                );
            }

            if (umiLength <= 0) {
                throw new CommandLine.ParameterException(
                        new CommandLine(this),
                        "-umiLength must be > 0"
                );
            }
        }
    }
}