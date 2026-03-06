package com.example;

public class Statistics {
    public static int[] umiEdits;
    public static int[] AnchorEdits;
    public static int largestUmiCluster = 0;
    public static String mostCommonUmi="";
    public static int largestAnchorCluster = 0;
    public static String mostCommonAnchor="";
    public static int largestUmiAnchorCluster = 0;
    public static byte[] mostCommonCombUmi;
    public static byte[] mostCommonCombAnchor;

    public static void incrementUmiPos(int position){
        umiEdits[position]++;
    }
    public static void incrementAnchorPos(int position){
        AnchorEdits[position]++;
    }
    public static void incrementLargestUmiCluster(int size, String umi){
        if(largestUmiCluster < size){
            largestUmiCluster = size;
            mostCommonUmi = umi;
        }
    }
    public static void incrementLargestAnchorCluster(int size, String anchor){
        if(largestAnchorCluster < size){
            largestAnchorCluster = size;
            mostCommonAnchor = anchor;
        }
    }
    public static void incrementLargestUmiAnchorCluster(int size, byte[] umi, byte[] anchor){
        if(largestUmiAnchorCluster < size){
            largestUmiAnchorCluster = size;
            mostCommonCombUmi = umi;
            mostCommonCombAnchor = anchor;
        }
    }
}
