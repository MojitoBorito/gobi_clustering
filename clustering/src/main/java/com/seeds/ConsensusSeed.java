package com.seeds;

import com.model.ClusterSeed;

import java.util.Arrays;

public class ConsensusSeed implements ClusterSeed<String> {
    private final int[][] counts;
    private final int length;
    private String cache;

    public ConsensusSeed(int length) {
            this.counts = new int[4][length];
            this.length = length;
    }


    @Override
    public void update(String newEntry) {
        cache = null;
        for (int i = 0; i < newEntry.length(); i++) {
            char base = newEntry.charAt(i);
            int baseIndex = baseIndex(base);
            if (baseIndex >= 0) {
                counts[baseIndex][i]++;
            }
        }
    }

    private int baseIndex(char base) {
        return switch (base) {
            case 'A' -> 0;
            case 'T' -> 1;
            case 'C' -> 2;
            case 'G' -> 3;
            default -> -1;
        };
    }

    @Override
    public String getValue() {
        if (cache != null) return cache;

        char[] consensus = new char[length];
        char[] bases = {'A', 'T', 'C', 'G'};

        for (int i = 0; i < length; i++) {
            int maxCount = 0;
            char maxBase = 'N'; // default to N if no counts
            for (int b = 0; b < 4; b++) {
                if (counts[b][i] > maxCount) {
                    maxCount = counts[b][i];
                    maxBase = bases[b];
                }
            }
            consensus[i] = maxBase;
        }

        cache = new String(consensus);
        return cache;
    }

}
