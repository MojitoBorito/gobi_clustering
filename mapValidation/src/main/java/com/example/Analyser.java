package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Analyser {

    HashMap<Integer, Set<String>> cluster2Header;
    HashMap<Integer, Set<String>> rp2Header;

    HashMap<String, Integer> header2cluster;
    HashMap<String, Integer> header2rp;

    public Analyser(String clusterFile, String BAMFile){
        cluster2Header = new HashMap<>();
        rp2Header = new HashMap<>();
        header2cluster = new HashMap<>();
        header2rp = new HashMap<>();

        buildClusterMaps(clusterFile);
        buildRpMaps(BAMFile);

        System.out.println("cluster2Header: " + cluster2Header.size());
        System.out.println("rp2Header: " + rp2Header.size());
        System.out.println("header2cluster: " + header2cluster.size());
        System.out.println("header2rp: " + header2rp.size());
    }

    public void buildClusterMaps(String clusterFile){
        try(BufferedReader br = new BufferedReader(new FileReader(clusterFile))){
            String line;
            while ((line = br.readLine()) != null){
                if (line.startsWith("ID")) continue;
                String[] lineSplit = line.split("\t");
                String[] headers = lineSplit[1].split("\\|");
                int id = Integer.parseInt(lineSplit[0]);
                cluster2Header.put(id, new HashSet<>(List.of(headers)));
                for (String header : headers) {
                    header2cluster.put(header, id);
                }
            }
        }catch (Exception e){
            System.err.println("Error reading cluster file");
        }
    }

    public void buildRpMaps(String BAMFile){
        BamReader bam = new BamReader(BAMFile);
        int i = 0;
        for (Map.Entry<ReadPair, Set<String>> entry : bam.pcrCluster.entrySet()) {
            rp2Header.put(i, entry.getValue());
            for (String header : entry.getValue()) {
                header2rp.put(header, i);
            }
            i++;
        }
    }

    public void validate(String out){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(out))){
            bw.write("header\tpredicted\tmapping\tcontains\tmatch\n");
            for(String header : header2cluster.keySet()){
                if(!header2rp.containsKey(header)){
                    continue;
                }
                int clusterID = header2cluster.get(header);
                int headerIdBAM = header2rp.get(header);

                Set<String> cluster = cluster2Header.get(clusterID);
                Set<String> bamCluster = rp2Header.get(headerIdBAM);

                boolean contains = intersect(bamCluster, cluster);
                boolean match = contains && bamCluster.size() == cluster.size();

                bw.write(header + "\t" + cluster.size() + "\t" + bamCluster.size() + "\t" + contains + "\t" + match + "\n");
            }
        }catch (Exception e){
            System.err.println("Error writing cluster file");
        }
    }

    public void validateClusterLevel(String out) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            bw.write("clusterID\tpredSize\tbamID\tbamSize\tintersectCount\tfracPredInBam\tfracBamInPred\texactMatch\tjaccard\n");

            // --- contingency accumulators (built while we iterate) ---
            Map<Integer, Map<Integer, Integer>> contingency = new HashMap<>();
            long totalMapped = 0;

            for (Integer clusterID : cluster2Header.keySet()) {
                Set<String> predCluster = cluster2Header.get(clusterID);
                int bestBamID = -1;
                Set<String> bestBamCluster = Collections.emptySet();
                int maxIntersect = 0;
                double bestJaccard = 0.0;
                HashSet<Integer> processedBamIDs = new HashSet<>();

                Map<Integer, Integer> bamHits = new HashMap<>(); // bamID -> hit count for this predCluster

                for (String header : predCluster) {
                    Integer bamID = header2rp.getOrDefault(header, null);
                    if (bamID == null) continue;
                    bamHits.merge(bamID, 1, Integer::sum); // accumulate for contingency
                    if (!processedBamIDs.add(bamID)) continue;
                    Set<String> bamCluster = rp2Header.get(bamID);
                    Set<String> intersect = new HashSet<>(predCluster);
                    intersect.retainAll(bamCluster);
                    int intersectCount = intersect.size();
                    double jaccard = intersectCount * 1.0 / (predCluster.size() + bamCluster.size() - intersectCount);
                    if (intersectCount > maxIntersect || jaccard > bestJaccard) {
                        bestBamID = bamID;
                        bestBamCluster = bamCluster;
                        maxIntersect = intersectCount;
                        bestJaccard = jaccard;
                    }
                }

                // Fold bamHits into the global contingency table
                if (!bamHits.isEmpty()) {
                    contingency.put(clusterID, bamHits);
                    totalMapped += bamHits.values().stream().mapToInt(Integer::intValue).sum();
                }

                double fracPredInBam = maxIntersect * 1.0 / predCluster.size();
                double fracBamInPred = bestBamCluster.isEmpty() ? 0 : maxIntersect * 1.0 / bestBamCluster.size();
                boolean exactMatch = maxIntersect == predCluster.size() && predCluster.size() == bestBamCluster.size();
                bw.write(clusterID + "\t" + predCluster.size() + "\t" + bestBamID + "\t" +
                        bestBamCluster.size() + "\t" +
                        maxIntersect + "\t" +
                        fracPredInBam + "\t" + fracBamInPred + "\t" + exactMatch + "\t" + bestJaccard + "\n");
            }

            // --- global metrics from the contingency table we built above ---
            if (totalMapped > 0) {
                double[] metrics = computeMetricsFromContingency(contingency, totalMapped);
                System.out.printf("Homogeneity=%.4f \nCompleteness=%.4f \nV-measure=%.4f \nARI=%.4f \nprecision:%.4f \nrecall:%.4f%n",
                        metrics[0], metrics[1], metrics[2], metrics[3], metrics[4],  metrics[5]);
            }

        } catch (Exception e) {
            System.err.println("Error writing cluster file");
            e.printStackTrace();
        }
    }

    private double[] computeMetricsFromContingency(Map<Integer, Map<Integer, Integer>> contingency, long N) {
        Map<Integer, Long> predCounts = new HashMap<>();
        Map<Integer, Long> bamCounts  = new HashMap<>();

        for (var pe : contingency.entrySet()) {
            for (var be : pe.getValue().entrySet()) {
                predCounts.merge(pe.getKey(),    (long) be.getValue(), Long::sum);
                bamCounts.merge(be.getKey(),     (long) be.getValue(), Long::sum);
            }
        }

        // --- Homogeneity/Completeness/V-measure as before ---
        double hClassGivenCluster = 0.0;
        double hClusterGivenClass = 0.0;
        double hClass   = 0.0;
        double hCluster = 0.0;

        // Reverse contingency for H(K|C)
        Map<Integer, Map<Integer, Integer>> reverse = new HashMap<>();
        for (var pe : contingency.entrySet())
            for (var be : pe.getValue().entrySet())
                reverse.computeIfAbsent(be.getKey(), k -> new HashMap<>())
                        .merge(pe.getKey(), be.getValue(), Integer::sum);

        for (var pe : contingency.entrySet()) {
            long predTotal = predCounts.get(pe.getKey());
            for (int count : pe.getValue().values()) {
                double p = count / (double) N;
                hClassGivenCluster -= p * (Math.log(count) - Math.log(predTotal));
            }
        }
        for (var be : reverse.entrySet()) {
            long bamTotal = bamCounts.get(be.getKey());
            for (int count : be.getValue().values()) {
                double p = count / (double) N;
                hClusterGivenClass -= p * (Math.log(count) - Math.log(bamTotal));
            }
        }
        for (long c : bamCounts.values())  { double p = c / (double) N; hClass   -= p * Math.log(p); }
        for (long c : predCounts.values()) { double p = c / (double) N; hCluster -= p * Math.log(p); }

        double homogeneity  = hClass   < 1e-10 ? 1.0 : 1.0 - hClassGivenCluster / hClass;
        double completeness = hCluster < 1e-10 ? 1.0 : 1.0 - hClusterGivenClass / hCluster;
        double vMeasure     = (homogeneity + completeness) < 1e-10 ? 0.0
                : 2.0 * homogeneity * completeness / (homogeneity + completeness);

        // --- ARI as before ---
        long sumCij = 0, sumAi = 0, sumBj = 0;
        for (var pe : contingency.entrySet())
            for (int c : pe.getValue().values()) sumCij += (long) c * (c - 1) / 2;
        for (long c : predCounts.values()) sumAi += c * (c - 1) / 2;
        for (long c : bamCounts.values())  sumBj += c * (c - 1) / 2;
        long total = N * (N - 1) / 2;
        double expected = (double) sumAi * sumBj / total;
        double maxPart  = (sumAi + sumBj) / 2.0;
        double ari      = (maxPart - expected) < 1e-10 ? 1.0 : (sumCij - expected) / (maxPart - expected);

        // --- Precision and Recall ---
        double precision = sumAi == 0 ? 1.0 : (double) sumCij / sumAi;
        double recall    = sumBj == 0 ? 1.0 : (double) sumCij / sumBj;

        return new double[]{ homogeneity, completeness, vMeasure, ari, precision, recall };
    }

    public boolean intersect(Set<String> set1, Set<String> set2){
        for(String header : set1){
            if(!set2.contains(header)){
                return false;
            }
        }
        return true;
    }

    public void writeBamClusterSizes(String outfile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            bw.write("bamClusterID\theaders\tbamClusterSize\n");
            for (Map.Entry<Integer, Set<String>> entry : rp2Header.entrySet()) {
                Integer bamClusterID = entry.getKey();
                int size = entry.getValue().size();
                bw.write(bamClusterID + "\t" + String.join("|", entry.getValue()) + "\t" + size + "\n");
            }
        } catch (Exception e) {
            System.err.println("Error writing BAM cluster sizes: " + e.getMessage());
        }
    }
}
