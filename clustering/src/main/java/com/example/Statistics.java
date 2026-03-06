package com.example;

public class Statistics {
    public static int[] umiEdits;
    public static int[] AnchorEdits;
    public static int largestUmiCluster = 0;
    public static int largestAnchorCluster = 0;
    public static int largestUmiAnchorCluster = 0;

    public static void incrementUmiPos(int position){
        umiEdits[position]++;
    }
    public static void incrementAnchorPos(int position){
        AnchorEdits[position]++;
    }
    public static void incrementLargestUmiCluster(int size){
        if(largestUmiCluster < size){
            largestUmiCluster = size;
        }
    }
    public static void incrementLargestAnchorCluster(int size){
        if(largestAnchorCluster < size){
            largestAnchorCluster = size;
        }
    }
    public static void incrementLargestUmiAnchorCluster(int size){
        if(largestUmiAnchorCluster < size){
            largestUmiAnchorCluster = size;
        }
    }
}
