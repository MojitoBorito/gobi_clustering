package com.seeds;

import com.model.ClusterSeed;

public class AnchorSeed<V> implements ClusterSeed<V> {
    private V anchor;

    @Override
    public void update(V newEntry) {
        if (newEntry == null) throw new IllegalArgumentException("Anchor can't be null!");
        if (anchor == null) anchor = newEntry;
    }

    @Override
    public V getValue() {
        return anchor;
    }
}
