package com.encoding;

import com.kmer.KmerLongSet;

import java.util.Arrays;

/**
 * Minimizer-based encoder.
 *
 * For every window of w consecutive valid k-mers, emit the minimum encoded k-mer.
 * Ties are broken by keeping the leftmost occurrence.
 *
 * Notes:
 * - Only A/C/G/T are considered valid.
 * - Lowercase letters are accepted.
 * - A stretch containing invalid bases (e.g. N) breaks the k-mer stream.
 * - The output is sorted and deduplicated.
 */
public class MinimizerEncoder extends KmerEncoder<KmerLongSet> {

    private final int w;
    private final long[] lookup;

    public MinimizerEncoder(int k, int w) {
        super(k);

        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (k > 32) {
            throw new IllegalArgumentException("Max k-mer size for 2-bit encoding is 32");
        }
        if (w <= 0) {
            throw new IllegalArgumentException("w must be positive");
        }

        this.w = w;
        this.lookup = new long[128];
        Arrays.fill(this.lookup, -1L);

        // Use a consistent 2-bit encoding.
        // Here: A=0, C=1, G=2, T=3
        lookup['A'] = 0; lookup['a'] = 0;
        lookup['C'] = 1; lookup['c'] = 1;
        lookup['G'] = 2; lookup['g'] = 2;
        lookup['T'] = 3; lookup['t'] = 3;
    }

    public int w() {
        return w;
    }

    @Override
    public KmerLongSet encode(String sequence) {
        int k = k();
        int n = sequence.length();

        if (n < k) {
            return new KmerLongSet(new long[0]);
        }

        int maxKmers = n - k + 1;

        // Store only valid k-mers, plus their start positions in the original sequence.
        long[] validKmers = new long[maxKmers];
        int[] validPositions = new int[maxKmers];
        int validCount = 0;

        long mask = (k == 32) ? -1L : (1L << (2 * k)) - 1L;
        long rolling = 0L;
        int validBasesInRow = 0;

        // Step 1: generate all valid k-mers with rolling hash
        for (int i = 0; i < n; i++) {
            char c = sequence.charAt(i);
            long code = (c < 128) ? lookup[c] : -1L;

            if (code < 0) {
                rolling = 0L;
                validBasesInRow = 0;
                continue;
            }

            rolling = ((rolling << 2) | code) & mask;
            validBasesInRow++;

            if (validBasesInRow >= k) {
                int start = i - k + 1;
                validKmers[validCount] = rolling;
                validPositions[validCount] = start;
                validCount++;
            }
        }

        if (validCount == 0) {
            return new KmerLongSet(new long[0]);
        }

        // Step 2: sliding minimum over valid k-mers using a monotone deque
        // Deque stores indices into validKmers[].
        int[] deque = new int[validCount];
        int head = 0;
        int tail = 0;

        long[] minimizers = new long[validCount];
        int minCount = 0;

        for (int i = 0; i < validCount; i++) {
            // Maintain ascending deque.
            // IMPORTANT: use > and not >= so equal values keep the older (leftmost) one.
            while (tail > head && validKmers[deque[tail - 1]] > validKmers[i]) {
                tail--;
            }
            deque[tail++] = i;

            // Remove elements outside the candidate window of last w valid k-mers
            while (head < tail && deque[head] < i - w + 1) {
                head++;
            }

            // We only have a real minimizer window if:
            // 1) there are at least w valid k-mers
            // 2) those w k-mers are consecutive in the original sequence
            if (i >= w - 1) {
                int left = i - w + 1;

                boolean consecutive =
                        validPositions[i] - validPositions[left] == w - 1;

                if (consecutive && head < tail) {
                    long minVal = validKmers[deque[head]];

                    // Avoid consecutive duplicate emissions
                    if (minCount == 0 || minimizers[minCount - 1] != minVal) {
                        minimizers[minCount++] = minVal;
                    }
                }
            }
        }

        if (minCount == 0) {
            return new KmerLongSet(new long[0]);
        }

        // Step 3: sort and deduplicate globally
        long[] result = Arrays.copyOf(minimizers, minCount);
        Arrays.sort(result);

        int unique = 1;
        for (int i = 1; i < result.length; i++) {
            if (result[i] != result[i - 1]) {
                result[unique++] = result[i];
            }
        }

        return new KmerLongSet(Arrays.copyOf(result, unique));
    }
}