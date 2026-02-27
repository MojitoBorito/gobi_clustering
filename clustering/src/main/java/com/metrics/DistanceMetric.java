package com.metrics;

public interface DistanceMetric<E> {
    // Must be symmetric
    double compute(E e1, E e2);
}