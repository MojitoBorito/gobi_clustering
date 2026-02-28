package com.clustering;

import com.example.FASTQ;
import com.example.Sequence;
import com.linkage.AverageLinkage;
import com.metrics.Hamming;
import com.metrics.SmithWatermanDistance;
import com.model.Cluster;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import com.model.Universe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GreedyClusters<C extends Cluster<E>, E> {
    private final Universe<C, E> universe;
    private final double threshold;

    public GreedyClusters(Universe.ClusterFactory<C> factory,
                          DistanceMetric<E> metric,
                          ClusterLinkage<C, E> linkage,
                          double threshold) {
        this.universe = new Universe<>(factory, metric, linkage);
        this.threshold = threshold;
    }

    public void computeClusters(Iterator<E> elements) {
        E elem;
        int countIt = 0;
        while (elements.hasNext()) {
            countIt++;
            elem = elements.next();
            double minDist = Double.POSITIVE_INFINITY;
            C bestCluster = null;
            for (C cluster : universe) {
                if (cluster.isEmpty()) continue;
                double currentDist = universe.distanceToCluster(elem, cluster);
                if (currentDist < minDist) {
                    minDist = currentDist;
                    bestCluster = cluster;
                }
            }
            if (bestCluster == null || minDist >= threshold) {
                universe.createCluster().addElement(elem);
            } else {
                bestCluster.addElement(elem);
            }
            if (countIt % 1000 == 0) System.out.println(countIt);
        }
    }

    static void main() {
//        HashMap<String, Sequence> fast = FASTQ.readFastq("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/fw.fastq.gz").getFastq();
//        List<String> sequences = fast.values().stream().map(Sequence::getSequence).toList();
//        System.out.println(fast.size());
//        SmithWatermanDistance metric = new SmithWatermanDistance();
//        AverageLinkage<Cluster<String>, String> linkage = new AverageLinkage<>();
//        double threshold = 0.01;
//        GreedyClusters<Cluster<String>, String> greedy =
//                new GreedyClusters<>(Cluster::new, metric, linkage, threshold);
//        greedy.computeClusters(sequences.iterator());

    }
}
