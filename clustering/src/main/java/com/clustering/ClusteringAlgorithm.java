package com.clustering;

import com.bucket.SmartBuckets;
import com.linkage.ClusterLinkage;
import com.metrics.DistanceMetric;
import com.model.Cluster;
import com.model.Element;
import com.model.Universe;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

public abstract class ClusteringAlgorithm<V, C extends Cluster<V>> {
    protected final Universe<V, C> universe;

    public ClusteringAlgorithm(SmartBuckets<V, C> buckets,
                               Universe.ClusterFactory<C> factory,
                               DistanceMetric<V> metric,
                               ClusterLinkage<V, C> linkage) {
        this.universe = new Universe<>(buckets, factory, metric, linkage);
    }

    public abstract void computeClusters(Iterator<? extends Element<V>> elements);

    public void writeClusters() {}

//    public void writeClustersCompact(Path path) {
//        try (BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(path, StandardOpenOption.CREATE), 512 * 1024)) {
//            StringBuilder sb = new StringBuilder();
//            for (Cluster<V> cluster : universe) {
//                List<String> ids = cluster.getElementIds();
//                if (ids.isEmpty()) continue;
//                sb.append(String.join("\t", ids));
//                sb.append('\n');
//            }
//            writer.write(sb.toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
