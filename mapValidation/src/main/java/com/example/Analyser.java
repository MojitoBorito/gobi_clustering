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

    public void validateClusterLevel(String out){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(out))){
            bw.write("clusterID\tpredSize\tbamID\tbamSize\tintersectCount\tfracPredInBam\tfracBamInPred\texactMatch\tjaccard\n");
            for(Integer clusterID : cluster2Header.keySet()){
                Set<String> predCluster = cluster2Header.get(clusterID);
                int bestBamID = -1;
                Set<String> bestBamCluster = Collections.emptySet();
                int maxIntersect = 0;
                double bestJaccard = 0.0;
                HashSet<Integer> processedBamIDs = new HashSet<>();
                for (String header : predCluster) {
                    Integer bamID = header2rp.getOrDefault(header, null);
                    if (bamID == null) continue;
                    if (!processedBamIDs.add(bamID)) continue; // only process each BAM cluster once
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
                double fracPredInBam = maxIntersect * 1.0 / predCluster.size();
                double fracBamInPred = bestBamCluster.isEmpty() ? 0 : maxIntersect * 1.0 / bestBamCluster.size();
                boolean exactMatch = maxIntersect == predCluster.size() && predCluster.size() == bestBamCluster.size();
                bw.write(clusterID + "\t" + predCluster.size() + "\t" + bestBamID + "\t" +
                        bestBamCluster.size() + "\t" +
                        maxIntersect + "\t" +
                        fracPredInBam + "\t" + fracBamInPred + "\t" + exactMatch + "\t" + bestJaccard + "\n");
            }
        }catch (Exception e){
            System.err.println("Error writing cluster file");
            e.printStackTrace();
        }
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
            bw.write("bamClusterID\tbamClusterSize\n");
            for (Map.Entry<Integer, Set<String>> entry : rp2Header.entrySet()) {
                Integer bamClusterID = entry.getKey();
                int size = entry.getValue().size();
                bw.write(bamClusterID + "\t" + size + "\n");
            }
        } catch (Exception e) {
            System.err.println("Error writing BAM cluster sizes: " + e.getMessage());
        }
    }
}
