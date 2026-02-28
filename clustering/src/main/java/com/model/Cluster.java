package com.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Cluster<V> {
    private final int id;
    private final List<String> elementIds;

    protected Cluster(int id) {
        this.id = id;
        this.elementIds = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int size() {
        return elementIds.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void addElement(String id, V value) {
        elementIds.add(id);
        onElementAdded(value);
    }

    protected abstract void onElementAdded(V value);
}
