package GenomicStructure;

import ReadSimulation.RandomUtils;

import java.util.*;

public class Sequence {
    private final StringBuilder sequence;
    private final Random random;
    private final String id;
    private List<Integer> mutations = new ArrayList<>();
    private RegionVector regions; // Genomic regions the sequence was created from
    public boolean mutant = false;

    private static final int DEFAULT_PHRED = 40;   // high quality for unchanged bases
    private static final int MUTATION_PHRED = 10;  // lower quality for mutated bases

    public Sequence(String id, StringBuilder sequence, Random random, RegionVector regions) {
        this.id = id;
        this.sequence = sequence;
        this.random = random;
        this.regions = regions;
    }

    public String getId() {
        return id;
    }

    private void mutateBase(int index) {
        int mutIndex = random.nextInt(0, 3);
        switch (sequence.charAt(index)) {
            case 'A':
                sequence.setCharAt(index, "TCG".charAt(mutIndex));
                return;
            case 'T':
                sequence.setCharAt(index, "ACG".charAt(mutIndex));
                return;
            case 'C':
                sequence.setCharAt(index, "ATG".charAt(mutIndex));
                return;
            case 'G':
                sequence.setCharAt(index, "ATC".charAt(mutIndex));
        }
    }

    // End exclusive
    public Sequence subSequence(String id, int start, int end) {
        return new Sequence(id,
                new StringBuilder(sequence.subSequence(start, end)),
                random,
                regions.subset(start, end)
        );
    }

    public Sequence reverseComplement(boolean inverseRegions) {
        StringBuilder out = new StringBuilder();
        for (int i = sequence.length() - 1; i >= 0; i--) {
            out.append(getComplement(sequence.charAt(i)));
        }
        if (inverseRegions) {
            return new Sequence(id, out, random, regions.reversedCopy());
        } else {
            return new Sequence(id, out, random, regions.copy());
        }
    }

    private char getComplement(char base) {
        return switch (base) {
            case 'A' -> 'T';
            case 'T' -> 'A';
            case 'C' -> 'G';
            case 'G' -> 'C';
            default -> base;
        };
    }

    public void mutate(double rate) {
        int numOfMutations = RandomUtils.samplePoisson(random, (rate / 100) * length());
        List<Integer> pointMutationIndices;

        if ((double) numOfMutations / length() <= 0.2) {
            pointMutationIndices = RandomUtils.generateDistinct_HashSet(random, numOfMutations, length());
        } else {
            pointMutationIndices = RandomUtils.generateDistinct_Shuffle(random, numOfMutations, length());
        }

        for (int index : pointMutationIndices) {
            mutant = true;
            mutateBase(index);
        }

        mutations.addAll(pointMutationIndices);
    }

    public void mutate2(double rate) {
        for (int i = 0; i < length(); i++) {
            if (random.nextInt(10000) < rate * 100) {
                mutations.add(i);
                mutateBase(i);
            }
        }
    }

    public int length() {
        return sequence.length();
    }

    public String getMutations() {
        if (mutations.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mutations.get(0));
        for (int i = 1; i < mutations.size(); i++) {
            sb.append(",").append(mutations.get(i));
        }
        return sb.toString();
    }

    public RegionVector getRegionVector() {
        return regions;
    }

    private static char phredToAscii(int phred) {
        return (char) (phred + 33); // FASTQ Phred+33
    }

    private String buildQualityString() {
        char[] qualities = new char[length()];
        Arrays.fill(qualities, phredToAscii(DEFAULT_PHRED));

        for (int pos : mutations) {
            if (pos >= 0 && pos < qualities.length) {
                qualities[pos] = phredToAscii(MUTATION_PHRED);
            }
        }

        return new String(qualities);
    }

    @Override
    public String toString() {
        return getInFastaFormat(id, sequence.toString(), mutations);
    }

    public static String getInFastaFormat(String id, String sequence, List<Integer> mutations) {
        int length = sequence.length();

        char[] qualities = new char[length];
        Arrays.fill(qualities, phredToAscii(DEFAULT_PHRED));

        if (mutations != null) {
            for (int pos : mutations) {
                if (pos >= 0 && pos < length) {
                    qualities[pos] = phredToAscii(MUTATION_PHRED);
                }
            }
        }

        return "@" + id + "\n"
                + sequence + "\n"
                + "+" + id + "\n"
                + new String(qualities) + "\n";
    }
}