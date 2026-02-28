package com.clustering;

import com.model.Cluster;
import com.model.Universe;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public abstract class ClusteringAlgorithm<C extends Cluster<V>, V> {
    public abstract void computeClusters(Iterator<V> elements);

    public void writeClusters(Universe<C, V> universe) {}

//    public void writeClustersCompact(Universe<C, V> universe, Path path) {
//        try (BufferedWriter writer = new BufferedWriter((Files.newBufferedWriter(path, StandardOpenOption.CREATE)), 512 * 1024)) {
//            for (Cluster<V> cluster : universe) {
//                StringBuilder sb = new StringBuilder(cluster.size());
//                for (Element<V> element : cluster) {
//                    sb.append(element.getId()).append('\t');
//                }
//                sb.deleteCharAt(sb.length()-1); // remove last tab
//                writer.write(sb.toString());
//                writer.write('\n');
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
