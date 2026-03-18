package ReadSimulation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import GenomicStructure.*;
import FileUtils.*;
import org.apache.commons.math3.distribution.BinomialDistribution;

public class ReadSimulator {
    static Random random = new Random();


    public static void simulateReads(String gtf,
                                     String fasta,
                                     String fai,
                                     String readCounts,
                                     String od,
                                     int readLength,
                                     int fragmentLength,
                                     int sd,
                                     double mutationRate,
                                     Double pcrEfficiency,
                                     Integer flowCellCapacity,
                                     Integer numOfCycles,
                                     Integer umiLength,
                                     Integer numOfUmis) throws IOException {
        System.out.println("Running");
        int umiNum = numOfUmis == null ? flowCellCapacity : numOfUmis;
        boolean amplify = pcrEfficiency != null;
        HashMap<String, Integer> counts = CountsReader.getCounts(readCounts);
        HashMap<String, Gene> genes = GTFUtils.readGTF(gtf, counts.keySet());
        FastaReader fastaReader = new FastaReader(fasta, fai);

        double samplingRate = 1.0;
        if (amplify) {
            double expectedCopiesPerFragment = Math.pow(1.0 + pcrEfficiency / 100.0, numOfCycles);
            long totalFragments = counts.values().stream().mapToLong(Integer::intValue).sum();
            double expectedTotalMolecules = totalFragments * expectedCopiesPerFragment;
            samplingRate = Math.min(1.0, (double) flowCellCapacity / expectedTotalMolecules);
        }

        // Count current total number of reads generated to use for readID
        int totalCount = 0;

        // Open writers
        try (BufferedWriter fw = new BufferedWriter(Files.newBufferedWriter(Path.of(od, "fw.fastq"),
                StandardOpenOption.CREATE), 512 * 1024);
             BufferedWriter rv = new BufferedWriter(Files.newBufferedWriter(Path.of(od, "rw.fastq"),
                     StandardOpenOption.CREATE), 512 * 1024);
             BufferedWriter map = new BufferedWriter(Files.newBufferedWriter(Path.of(od, "read.mappinginfo"),
                     StandardOpenOption.CREATE), 512 * 1024);
             BufferedWriter umi = amplify
                     ? new BufferedWriter(Files.newBufferedWriter(Path.of(od, "umi.fastq"), StandardOpenOption.CREATE), 512 * 1024)
                     : null) {


            UmiFactory umiFactory = null;
            if (pcrEfficiency != null) {
                umiFactory = new UmiFactory(random, umiLength, umiNum);
            }

            String header = "readid\tchr_id\tgene_id\ttranscript_id\tfw_regvec\trw_regvec\tt_fw_regvec\tt_rw_regvec\tfw_mut\trw_mut";
            map.write(header);

            for (Gene gene : genes.values()) {
                //String geneSequence = fastaReader.getSequence(gene.getChr(), gene.getStart(), gene.getEnd());
                Iterator<Sequence> transcriptSeqs = trascriptSeqs(gene, fastaReader);
                while (transcriptSeqs.hasNext()) {

                    Sequence transcript = gene.isPositiveStrand() ? transcriptSeqs.next() : transcriptSeqs.next().reverseComplement(false);

                    // Can't generate reads from transcript that is smaller than read length
                    if (transcript.length() < readLength) {
                        continue;
                    }

                    Sequence rTranscript = transcript.reverseComplement(true);
                    int count = counts.getOrDefault(transcript.getId(), -1);

                    // Generate fragmentLengths and startPositions
                    int[] fragmentLengths = generateFragmentLengths(fragmentLength, sd, readLength, count, transcript.length());
                    int[] startPositionsFW = generateStartPositions(transcript.length(), fragmentLengths, count);
                    int[] startPositionRV = new int[count];

                    for (int i = 0; i < count; i++) {
                        startPositionRV[i] = transcript.length() - (startPositionsFW[i] + fragmentLengths[i]);
                    }

                    if (!gene.isPositiveStrand()) {
                        int[] tmpS = startPositionsFW;
                        startPositionsFW = startPositionRV;
                        startPositionRV = tmpS;
                    }


                    // Get reads
                    Iterator<Sequence> fwReads;
                    Iterator<Sequence> rvReads;
                    int[] fragmentDuplicateCounts = null;
                    if (amplify) {
                        fragmentDuplicateCounts = sampleFragmentCounts(generateFragmentDuplicateCounts(count, pcrEfficiency, numOfCycles), samplingRate);
                        fwReads = generateReadsAmplified(transcript, startPositionsFW, readLength, count, totalCount, fragmentDuplicateCounts.clone(), mutationRate);
                        rvReads = generateReadsAmplified(rTranscript, startPositionRV, readLength, count, totalCount, fragmentDuplicateCounts.clone(), mutationRate);

                    } else {
                        fwReads = generateReads(transcript, startPositionsFW, readLength, count, totalCount, mutationRate);
                        rvReads = generateReads(rTranscript, startPositionRV, readLength, count, totalCount, mutationRate);
                    }

                    // Write to files
                    // fwReads and rvReads should have the same size
                    int currentFragment = 0;
                    String umiSeq = null;

                    if (amplify) {
                        umiSeq = umiFactory.getUmi();
                    }

                    while (fwReads.hasNext() && rvReads.hasNext()) {

                        if (totalCount % 10_000 == 0) System.out.println(totalCount);

                        while (amplify && fragmentDuplicateCounts[currentFragment] <= 0) {
                            currentFragment++;
                        }

                        Sequence fwRead = fwReads.next();
                        Sequence rvRead = rvReads.next();

                        fw.write(fwRead.toString());
                        rv.write(rvRead.toString());

                        StringBuilder sb = new StringBuilder();
                        sb.append('\n');
                        sb.append(fwRead.getId()).append("\t")                                                          // readID
                                .append(gene.getChr()).append("\t")                                                     // chr_id
                                .append(gene.getId()).append("\t")                                                      // gene_id
                                .append(transcript.getId()).append("\t")                                                // transcript_id
                                .append(fwRead.getRegionVector().toString()).append("\t")                               // fw_regvec
                                .append(rvRead.getRegionVector().toString()).append("\t")                               // rw_regvec
                                .append(startPositionsFW[currentFragment])                                                  // t_fw_regvec
                                .append('-')
                                .append(startPositionsFW[currentFragment] + readLength).append("\t")
                                .append(startPositionsFW[currentFragment] + fragmentLengths[currentFragment] - readLength)      // t_rw_regvec
                                .append('-')
                                .append(startPositionsFW[currentFragment] + fragmentLengths[currentFragment]).append("\t")
                                .append(fwRead.getMutations()).append("\t")                                             // fw_mutations
                                .append(rvRead.getMutations());                                                         // rw_mutations
                        map.write(sb.toString());
                        sb.setLength(0);

                        if (amplify) {
                            umi.write(Sequence.getInFastaFormat(fwRead.getId(), umiSeq, null));
                        }

                        if (!amplify || --fragmentDuplicateCounts[currentFragment] <= 0) {
                            currentFragment++;
                            if (amplify) umiSeq = umiFactory.getUmi();
                        }
                        totalCount++;
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Get all transcripts for a given gene
    private static Iterator<Sequence> trascriptSeqs(Gene gene, FastaReader reader) {
        return new Iterator<>() {
            final LinkedList<Transcript> transcripts = new LinkedList<>(gene.getTranscripts().values());
            @Override
            public boolean hasNext() {
                return !transcripts.isEmpty();
            }

            @Override
            public Sequence next() {
                StringBuilder sb = new StringBuilder();
                Transcript transcript = transcripts.removeFirst();
                try {
                    String entireSeq = reader.getSequence(gene.getChr(), transcript.getStart(), transcript.getEnd());

                    // This assumes exons are sorted
                    List<Region> exons = transcript.getExons();
                    if (transcript.isFwStrand()) {
                        for (Region exon : exons) {
                            sb.append(entireSeq, exon.getAbsoluteStart() - transcript.getStart(), exon.getAbsoluteEnd() - transcript.getStart() + 1);
                        }
                    } else {
                        for (int i = exons.size()-1; i >=0; i--) {
                            Region exon = exons.get(i);
                            sb.append(entireSeq, exon.getAbsoluteStart() - transcript.getStart(), exon.getAbsoluteEnd() - transcript.getStart() + 1);
                        }
                    }
                    return new Sequence(transcript.getId(), sb, random, transcript.getRegionVector());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    // Reads are generated in one direction (forward)
    // ReadTotalCount denotes the total number of generated reads (used for readIDs)
    private static Iterator<Sequence> generateReads(Sequence transcriptSeq, int[] startPositions, int readLength, int readCount, int readTotalCount, double mutationRate) {
        return new Iterator<>() {
            int currentRead = 0;

            @Override
            public boolean hasNext() {
                return currentRead < readCount;
            }

            @Override
            public Sequence next() {
                Sequence read = transcriptSeq.subSequence(String.valueOf(readTotalCount + currentRead), startPositions[currentRead], startPositions[currentRead] + readLength);
                read.mutate(mutationRate);
                currentRead++;
                return read;
            }
        };
    }

    private static Iterator<Sequence> generateReadsAmplified(Sequence transcriptSeq,
                                                             int[] startPositions,
                                                             int readLength,
                                                             int fragmentCount,
                                                             int readTotalCount,
                                                             int[] fragmentDuplicateCounts,
                                                             double mutationRate) {

        return new Iterator<>() {
            int currentFragment = 0;
            int currentRead = 0;

            @Override
            public boolean hasNext() {
                while (currentFragment < fragmentCount && fragmentDuplicateCounts[currentFragment] <= 0) {
                    currentFragment++;
                }

                return currentFragment < fragmentCount;
            }

            @Override
            public Sequence next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                Sequence read = transcriptSeq.subSequence(String.valueOf(readTotalCount + currentRead), startPositions[currentFragment], startPositions[currentFragment] + readLength);
                if (--fragmentDuplicateCounts[currentFragment] <= 0) {
                    currentFragment++;
                }
                read.mutate(mutationRate);
                currentRead++;
                return read;
            }
        };

    }
    private static int[] generateFragmentDuplicateCounts(int numOfFragments, double efficiency, int numOfCycles) {
        int[] copies = new int[numOfFragments];
        double p = efficiency/100;

        for (int i = 0; i < copies.length; i++) {
            int count = 1;

            for (int j = 0; j < numOfCycles; j++) {
                BinomialDistribution binomial = new BinomialDistribution(count, p);
                try {
                    count = Math.addExact(binomial.sample(), count);
                } catch (Exception e) {
                    throw new RuntimeException("" + count, e);
                }
            }

            copies[i] = count;
        }

        return copies;
    }

    private static int[] sampleFragmentCounts(int[] rawCounts, double samplingRate) {
        int[] sampled = new int[rawCounts.length];
        for (int i = 0; i < rawCounts.length; i++) {
            if (rawCounts[i] > 0) {
                sampled[i] = new BinomialDistribution(rawCounts[i], samplingRate).sample();
            }
        }
        return sampled;
    }

    private static int[] generateFragmentLengths(int mean, int sd, int readLength, int readCount ,int transcriptLength) {
        int[] lengths = new int[readCount];
        for (int i = 0; i < readCount; i++) {
            int rand = (int) (random.nextGaussian()*sd + mean);
            while (rand > transcriptLength || rand < readLength) {
                rand = (int) (random.nextGaussian() * sd + mean);
            }
            lengths[i] = rand;
        }
        return lengths;
    }

    private static int[] generateStartPositions(int transcriptLength, int[] fragmentLengths, int readCount) {
        int[] rands = new int[readCount];
        for (int i = 0; i < readCount; i++) {
            rands[i] = random.nextInt(transcriptLength - fragmentLengths[i] + 1); // +1 bc exclusive
        }
        return rands;
    }


}
