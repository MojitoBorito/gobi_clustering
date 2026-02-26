package com.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Universe<C extends Cluster<E>, E> implements Iterable<C>{
    private final ArrayList<C> clusters;
    private final DistanceMetric<E> metric;
    private final ClusterLinkage<C, E> linkage;
    private final ClusterFactory<C> factory;
    private int nextId = 0;

    public Universe(ClusterFactory<C> factory,
                    DistanceMetric<E> metric,
                    ClusterLinkage<C, E> linkage) {
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


    public double distanceToCluster(E elem, C cluster) {
        return linkage.distanceToCluster(metric, elem, cluster);
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
