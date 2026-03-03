package com.model;

import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;

import java.util.ArrayList;
import java.util.Iterator;

public class Universe<V, C extends Cluster<V>> implements Iterable<C>{
    private final ArrayList<C> clusters;
    private final DistanceMetric<V> metric;
    private final ClusterLinkage<V, C> linkage;
    private final ClusterFactory<C> factory;
    private int nextId = 0;

    public Universe(ClusterFactory<C> factory,
                    DistanceMetric<V> metric,
                    ClusterLinkage<V, C> linkage) {
        this.clusters = new ArrayList<>();
        this.factory = factory;
        this.metric = metric;
        this.linkage = linkage;
    }

    public C createCluster() {
        C newCluster = factory.create(nextId);
        clusters.add(nextId++, newCluster);
        return newCluster;
    }


    public double distanceToCluster(V value, C cluster) {
        return linkage.distanceToCluster(metric, value, cluster);
    }

    public double distanceBetweenClusters(C a, C b) {
        return linkage.distanceBetweenClusters(a, b);
    }

    @Override
    public Iterator<C> iterator() {
        return clusters.iterator();
    }


    @FunctionalInterface
    public interface ClusterFactory<C> {
        C create(int id);
    }
}
