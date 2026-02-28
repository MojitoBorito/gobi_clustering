package com.clustering;

import com.model.Cluster;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import com.model.Universe;

import java.util.Iterator;

public class GreedyClusters<C extends Cluster<V>, V> {
    private final Universe<C, V> universe;
    private final double threshold;

    public GreedyClusters(Universe.ClusterFactory<C> factory,
                          DistanceMetric<V> metric,
                          ClusterLinkage<C, V> linkage,
                          double threshold) {
        this.universe = new Universe<>(factory, metric, linkage);
        this.threshold = threshold;
    }

    public void computeClusters(Iterator<V> elements) {
//        Element<V> elem;
//        int count = 0;
//        while (elements.hasNext()) {
//            elem = elements.next();
//            double minDist = Double.POSITIVE_INFINITY;
//            C bestCluster = null;
//            for (C cluster : universe) {
//                if (cluster.isEmpty()) continue;
//                double currentDist = universe.distanceToCluster(elem, cluster);
//                if (currentDist < minDist) {
//                    minDist = currentDist;
//                    bestCluster = cluster;
//                }
//            }
//            if (bestCluster == null || minDist >= threshold) {
//                universe.createCluster().addElement(elem);
//            } else {
//                bestCluster.addElement(elem);
//            }
//            count++;
//            if (count % 1000 == 0) System.out.println(count);
//        }
    }

    static void main() {
        //HashMap<String, Sequence> fast = FASTQ.readFastq("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/fw.fastq.gz").getFastq();
        Long num = 300L;
        long time = System.nanoTime();
        num.hashCode();
        long end = System.nanoTime();
        System.out.println(end - time);
    }
}
