package com.metrics;

public class Hamming implements DistanceMetric<String>{

    // TODO: Implement early exit
    public Hamming() {}

    @Override
    public double compute(String e1, String e2) {
        if (e1.length() != e2.length()) throw new IllegalArgumentException("Unequal lengths");
        int dist = 0;
        int len = e1.length();

        for (int i = 0; i < len; i++) {
            dist += (e1.charAt(i) == e2.charAt(i) ? 0 : 1);
        }
        return (double) dist /len;
    }
}
