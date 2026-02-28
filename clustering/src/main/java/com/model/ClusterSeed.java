package com.model;

public interface ClusterSeed<V> {
    void update(V newEntry);
    V getValue();
}
