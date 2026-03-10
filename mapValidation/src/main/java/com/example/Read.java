package com.example;

import htsjdk.samtools.AlignmentBlock;

import java.util.List;

public class Read extends ReadInterval{
    public String readId;
    public ReadInterval[] alignments;
    public char strand;
    public int mismatchCount;
    public int clippingSize;
    public int splitCount;
    public boolean isFirstOfPair;

    public Read(String contig, int start, int end, char strand,
                        boolean isFirstOfPair, int mismatchCount,
                        int clippingSize, int splitCount,
                        List<AlignmentBlock> alignments, String readId) {
        super(contig, start, end);
        this.strand = strand;
        this.isFirstOfPair = isFirstOfPair;
        this.mismatchCount = mismatchCount;
        this.clippingSize = clippingSize;
        this.splitCount = splitCount;
        this.alignments = convertBlocksToArray(contig, alignments);
        this.readId = readId;
    }

    @Override
    public String toString() {
        return readId + ": " + "[" + start +":"+end+"]" + strand;
    }
}
