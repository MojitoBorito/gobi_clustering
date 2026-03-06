package com.model;

public interface Encoder<V, K> {
    K encode(V value);
}
