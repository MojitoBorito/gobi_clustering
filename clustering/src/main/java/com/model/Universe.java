package com.model;

import java.util.HashMap;
import java.util.Iterator;

public class Universe<K, C extends Cluster<K, E>, E> implements Iterable<C>{
    private final HashMap<K, C> clusters;
    private final DistanceMetric<E> metric;
    private final ClusterLinkage<K, C, E> linkage;
    private final ClusterFactory<K, C> factory;

    public Universe(ClusterFactory<K, C> factory,
                    DistanceMetric<E> metric,
                    ClusterLinkage<K, C, E> linkage) {
        this.clusters = new HashMap<>();
        this.factory = factory;
        this.metric = metric;
        this.linkage = linkage;
    }

    public C createCluster(K id) {
        if (clusters.containsKey(id)) throw new RuntimeException("Cluster already exists");
        C newCluster = factory.create(id);
        clusters.put(id, newCluster);
        return newCluster;
    }


    public double distanceToCluster(E elem, C cluster) {
        return linkage.distanceToCluster(metric, elem, cluster);
    }

    public double distanceBetweenClusters(C a, C b) {
        return linkage.distanceBetweenClusters(a, b);
    }

    @Override
    public Iterator<C> iterator() {
        return clusters.values().iterator();
    }


    @FunctionalInterface
    public interface ClusterFactory<K, C> {
        C create(K id);
    }
}
