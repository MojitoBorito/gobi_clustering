package com.bucket;

public record BucketStats<K>(int totalBuckets, double avgBucketSize, int maxBucketSize, K worstKey) {};
