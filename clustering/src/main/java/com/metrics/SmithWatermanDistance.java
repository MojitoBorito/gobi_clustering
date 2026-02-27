package com.metrics;

import org.biojava.nbio.alignment.SmithWaterman;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;

public final class SmithWatermanDistance implements DistanceMetric<String> {

    private final GapPenalty gap;
    private final SubstitutionMatrix<NucleotideCompound> matrix;

    public SmithWatermanDistance(GapPenalty gap,
                                 SubstitutionMatrix<NucleotideCompound> matrix) {
        this.gap = gap;
        this.matrix = matrix;
    }

    public SmithWatermanDistance() {
        this(new SimpleGapPenalty(), SubstitutionMatrixHelper.getNuc4_4());
    }

    @Override
    public double compute(String s1, String s2) {
        if (s1 == null || s2 == null) throw new NullPointerException();
        if (s1.isEmpty() && s2.isEmpty()) return 0.0;
        if (s1.isEmpty() || s2.isEmpty()) return 1.0;

        final double sab = symScore(s1, s2);
        final double saa = score(s1, s1);
        final double sbb = score(s2, s2);

        final double denom = saa + sbb;
        if (denom <= 0.0) {
            // Fallback: if scoring scheme makes self-scores non-positive (unusual for typical SW),
            // you can’t normalize this way.
            // In that case, treat "no positive similarity" as max distance.
            return 1.0;
        }

        double sim = (2.0 * sab) / denom;

        // numeric guardrails
        if (sim < 0.0) sim = 0.0;
        if (sim > 1.0) sim = 1.0;

        return 1.0 - sim;
    }

    private double symScore(String a, String b) {
        // Symmetrize to guarantee symmetry even if something subtle differs
        double ab = score(a, b);
        double ba = score(b, a);
        return 0.5 * (ab + ba);
    }

    private double score(String a, String b) {
        try {
            DNASequence qa = new DNASequence(a);
            DNASequence tb = new DNASequence(b);

            SmithWaterman<DNASequence, NucleotideCompound> sw =
                    new SmithWaterman<>(qa, tb, gap, matrix);

            return sw.getScore(); // local alignment score
        } catch (Exception e) {
            throw new RuntimeException("BioJava alignment failed", e);
        }
    }
}