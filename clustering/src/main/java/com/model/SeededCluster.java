package com.model;

public class SeededCluster<V> extends Cluster<V>{

    private ClusterSeed<V> seed;
    private final ClusterSeedFactory<V> seedFactory;

    public SeededCluster(int id, ClusterSeedFactory<V> seedFactory) {
        super(id);
        this.seedFactory = seedFactory;
    }

    @Override
    protected void onElementAdded(V value) {
        if (seed == null) initSeed();
        updateSeed(value);
    }

    public V getSeed(){
        return seed.getValue();
    }

    @FunctionalInterface
    public interface ClusterSeedFactory<V> {
        ClusterSeed<V> create();
    }

    protected void initSeed() {
        seed = seedFactory.create();
    }

    protected void updateSeed(V value) {
        seed.update(value);
    }

}
