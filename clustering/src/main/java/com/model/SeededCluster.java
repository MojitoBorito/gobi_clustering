package com.model;

public class SeededCluster<V> extends Cluster<V>{

    private ClusterSeed<V> seed;

    public SeededCluster(int id) {
        super(id);
    }

    @Override
    protected void onElementAdded(V value) {
        seed.update(value);
    }

    public V getSeed(){
        return seed.getValue();
    }

}
