package com.kmer;

import com.encoding.KmerEncoder;

import java.util.Arrays;

/**
 * Minimizer-based KmerEncoder.
 *
 * Slides a window of size w over the sequence's k-mers and picks
 * the lexicographically smallest k-mer in each window (the "minimizer").
 * Ties are broken by leftmost position.
 *
 * The result is a sorted, deduplicated long[] of minimizer k-mer values
 * wrapped in a KmerLongSet, so it plugs straight into BucketSet / Jaccard / etc.
 *
 * Parameters:
 *   k  – k-mer length  (must be ≤ 32 for 2-bit encoding)
 *   w  – window size    (number of consecutive k-mers per window;
 *                         the "super-mer" window is k + w − 1 bases long)
 */
public class MinimizerEncoder extends KmerEncoder {

    private final int w;          // window width (in k-mers)
    private final long[] lookup;  // base → 2-bit encoding

    /**
     * @param k k-mer size
     * @param w minimizer window size (number of k-mers in each window)
     */
    public MinimizerEncoder(int k, int w) {
        super(k);
        if (k > 32)
            throw new IllegalArgumentException("Max k-mer size for 2-bit encoding: 32");
        if (w <= 0)
            throw new IllegalArgumentException("Window size w must be positive");

        this.w = w;
        lookup = new long[128];
        Arrays.fill(lookup, -1);
        lookup['A'] = 0; lookup['T'] = 1; lookup['C'] = 2; lookup['G'] = 3;
    }

    public int w() {
        return w;
    }

    /**
     * Encode a DNA sequence into its set of minimizer k-mers.
     *
     * Algorithm:
     * 1. Compute all valid k-mer hashes using a rolling 2-bit window
     *    (identical to KmerLongSetEncoder).
     * 2. Slide a window of size w over those k-mers.
     *    In each window, pick the minimum value (= lexicographic minimum).
     *    We use a monotone-deque (ascending deque) so the whole pass is O(n).
     * 3. Deduplicate and sort the collected minimizers → KmerLongSet.
     */
    @Override
    public KmerLongSet encode(String sequence) {
        int k = k();

        // --- Step 1: compute all k-mer hashes (rolling) ---
        long mask = (k < 32) ? (1L << (2 * k)) - 1 : -1L; // full 64 bits when k==32
        long kmer = 0L;
        int valid = 0;

        // worst case: one k-mer per position
        int maxKmers = sequence.length() - k + 1;
        if (maxKmers <= 0) return new KmerLongSet(new long[0]);

        long[] kmers = new long[maxKmers];    // k-mer values
        boolean[] isValid = new boolean[maxKmers]; // tracks invalid (N, etc.)
        int kmerCount = 0;

        for (int i = 0; i < sequence.length(); i++) {
            long base = (sequence.charAt(i) < 128) ? lookup[sequence.charAt(i)] : -1;
            if (base < 0) {
                kmer = 0;
                valid = 0;
                // mark the position as invalid if we're in range
                if (i - k + 1 >= 0 && kmerCount < maxKmers) {
                    isValid[kmerCount] = false;
                    kmerCount++;
                }
                continue;
            }
            kmer = ((kmer << 2) | base) & mask;
            valid++;
            if (valid >= k) {
                kmers[kmerCount] = kmer;
                isValid[kmerCount] = true;
                kmerCount++;
            }
        }

        if (kmerCount == 0) return new KmerLongSet(new long[0]);

        // --- Step 2: sliding-window minimum using a monotone deque ---
        // The deque stores indices into kmers[]. Front = current minimum.
        int[] deque = new int[kmerCount];
        int dqHead = 0, dqTail = 0;

        // We'll collect minimizer values here (with possible duplicates)
        long[] minimizers = new long[kmerCount]; // upper bound on size
        int minCount = 0;

        for (int i = 0; i < kmerCount; i++) {
            if (!isValid[i]) {
                // invalid k-mer: clear the deque (window is broken)
                dqHead = 0;
                dqTail = 0;
                continue;
            }

            // Remove from back anything ≥ current value (maintain ascending deque)
            while (dqTail > dqHead && kmers[deque[dqTail - 1]] >= kmers[i]) {
                dqTail--;
            }
            deque[dqTail++] = i;

            // Remove from front anything outside the window [i - w + 1, i]
            while (dqHead < dqTail && deque[dqHead] < i - w + 1) {
                dqHead++;
            }

            // We have a full window once i >= w - 1
            if (i >= w - 1 && dqHead < dqTail) {
                long minVal = kmers[deque[dqHead]];
                // Avoid consecutive duplicate minimizers
                if (minCount == 0 || minimizers[minCount - 1] != minVal) {
                    minimizers[minCount++] = minVal;
                }
            }
        }

        // --- Step 3: sort + deduplicate → KmerLongSet ---
        if (minCount == 0) return new KmerLongSet(new long[0]);

        long[] result = Arrays.copyOf(minimizers, minCount);
        Arrays.sort(result);

        // deduplicate (result is sorted, so duplicates are adjacent)
        int unique = 1;
        for (int i = 1; i < result.length; i++) {
            if (result[i] != result[i - 1]) {
                result[unique++] = result[i];
            }
        }

        return new KmerLongSet(Arrays.copyOf(result, unique));
    }
}