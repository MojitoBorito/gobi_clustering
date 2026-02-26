package com.clustering;

import com.kmer.Jaccard;
import com.model.Cluster;
import com.model.ClusterLinkage;
import com.model.DistanceMetric;
import com.model.Universe;

import java.util.HashMap;
import java.util.Iterator;

public class GreedyClusters<K, C extends Cluster<K, E>, E> {
    private final Universe<K, C, E> universe;
    private final double threshold;

    public GreedyClusters(Universe.ClusterFactory<K, C> factory,
                          DistanceMetric<E> metric,
                          ClusterLinkage<K, C, E> linkage,
                          double threshold) {
        this.universe = new Universe<>(factory, metric, linkage);
        this.threshold = threshold;
    }

    public void computeClusters(Iterator<E> elements) {
        E elem;
        double minDist = Double.POSITIVE_INFINITY;
        C bestCluster = null;

        while (elements.hasNext()) {
            elem = elements.next();
            for (C cluster : universe) { /// Change so that hashmap isnt converted to collection in each iteration
                double currentDist = universe.distanceToCluster(elem, cluster);
                if (currentDist < minDist) {
                    minDist = currentDist;
                    bestCluster = cluster;
                }
            }
            if (minDist < threshold) {
                //universe.createCluster().addElement(elem);
            } else {
                bestCluster.addElement(elem);
            }
        }
    }

    static void main() {
    }
}
