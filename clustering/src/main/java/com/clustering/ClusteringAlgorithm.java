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
import java.util.*;

public abstract class ClusteringAlgorithm<K, V, C extends Cluster<V>> {
    protected final Universe<K, V, C> universe;

    public ClusteringAlgorithm(SmartBuckets<K, C> buckets,
                               Universe.ClusterFactory<C> factory,
                               DistanceMetric<V> metric,
                               ClusterLinkage<V, C> linkage) {
        this.universe = new Universe<>(buckets, factory, metric, linkage);
    }

    public abstract void computeClusters(Iterator<? extends Element<V>> elements);

    public void writeClusters() {}

    public void writeClustersCompact(Path path) {
        try (BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(path, StandardOpenOption.CREATE), 512 * 1024)) {
            writer.write("cluster_id");
            writer.write('\t');
            writer.write("read_id");
            writer.write('\n');
            Set<C> clusters = universe.getAllClusters();
            List<C> sortedClusters = clusters.stream().sorted(Comparator.comparingInt(Cluster::getId)).toList();
            for (Cluster<V> cluster : sortedClusters) {
                List<String> ids = cluster.getElementIds();
                if (ids.isEmpty()) continue;
                for (String id : ids) {
                    writer.write(String.valueOf(cluster.getId()));
                    writer.write('\t');
                    writer.write(String.valueOf(id));
                    writer.write('\n');
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<C> getAllClusters() {
        return universe.getAllClusters();
    }
}
