package com.example;

import htsjdk.samtools.AlignmentBlock;

import java.util.ArrayList;
import java.util.List;

public class ReadInterval{
    public String chr;
    public int start;
    public int end;


    public ReadInterval(String contig, int start, int end) {
        this.chr = contig;
        this.start = start;
        this.end = end;
    }

    public ReadInterval( int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static ReadInterval fromAlignmentBlock(String contig, AlignmentBlock block) {
        int start = block.getReferenceStart();
        int end = start + block.getLength() - 1;
        return new ReadInterval(contig, start, end);
    }

    public static ReadInterval[] convertBlocksToArray(String contig, List<AlignmentBlock> blocks) {
        if (blocks.size() == 1) {
            return new ReadInterval[] { ReadInterval.fromAlignmentBlock(contig, blocks.getFirst()) };
        }
        List<ReadInterval> result = new ArrayList<>();
        ReadInterval current = ReadInterval.fromAlignmentBlock(contig, blocks.getFirst());
        int start = current.start;
        int end = current.end;
        for (int i = 1; i < blocks.size(); i++) {
            ReadInterval next = ReadInterval.fromAlignmentBlock(contig, blocks.get(i));
            if (end + 1 >= next.start) {
                end = Math.max(end, next.end);
            } else {
                result.add(new ReadInterval(contig, start, end));
                start = next.start;
                end = next.end;
            }
        }
        result.add(new ReadInterval(contig, start, end));
        return result.toArray(new ReadInterval[0]);
    }


    public int getStart() {
        return start;
    }


    public int getStop() {
        return end;
    }

    public String getContig(){
        return chr;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReadInterval other)) return false;
        return this.chr.equals(other.chr) &&
                this.start == other. start &&
                this.end == other.end;
    }

    @Override
    public int hashCode() {
        int result = chr.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
}
