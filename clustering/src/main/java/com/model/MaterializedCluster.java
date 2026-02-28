package com.model;

import java.util.ArrayList;

public class MaterializedCluster<V> extends Cluster<V>{
    private final ArrayList<V> values = new ArrayList<>();

    public MaterializedCluster(int id) {
        super(id);
    }

    @Override
    protected void onElementAdded(V value) {
        values.add(value);
    }

    public ArrayList<V> getValues() {
        return values;
    }
}