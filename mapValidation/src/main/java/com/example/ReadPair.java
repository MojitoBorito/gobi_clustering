package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadPair {
    public Read first;
    public Read second;
    public char strand;
    private int cachedHashCode = 0;
    public ReadInterval[] mergedAlignments = null;

    public ReadPair(Read first, Read second) {
        this.strand = first.strand;
        if (shouldSwap(first, second)){
            this.first = second;
            this.second = first;
        }else{
            this.first = first;
            this.second = second;
        }
        getMergedAlignments();
        int result = first.chr.hashCode();
        result = 31 * result + (int) this.strand;
        result = 31 * result + Arrays.hashCode(mergedAlignments);
        cachedHashCode = result;
    }

    private boolean shouldSwap(Read first, Read second) {
        if (first.start != second.start) {
            return first.start > second.start;
        }
        if (first.end != second.end) {
            return first.end > second.end;
        }
        return false;
    }

    public void getMergedAlignments() {
        if (mergedAlignments == null) {
            mergedAlignments = mergeSortedAlignments(first.alignments, second.alignments);
        }
    }

    private ReadInterval[] mergeSortedAlignments(ReadInterval[] arr1, ReadInterval[] arr2) {
        List<ReadInterval> result = new ArrayList<>();
        int i = 0, j = 0;
        int mergedStart, mergedEnd;

        if (arr1[0].start <= arr2[0].start) {
            mergedStart = arr1[0].start;
            mergedEnd = arr1[0]. end;
            i++;
        } else {
            mergedStart = arr2[0]. start;
            mergedEnd = arr2[0].end;
            j++;
        }

        while (i < arr1.length || j < arr2.length) {
            ReadInterval next;

            if (i >= arr1.length) {
                next = arr2[j++];
            } else if (j >= arr2.length) {
                next = arr1[i++];
            } else if (arr1[i].start <= arr2[j].start) {
                next = arr1[i++];
            } else {
                next = arr2[j++];
            }

            if (mergedEnd + 1 >= next. start) {
                mergedEnd = Math.max(mergedEnd, next.end);
            } else {
                result.add(new ReadInterval(first.chr, mergedStart, mergedEnd));
                mergedStart = next.start;
                mergedEnd = next.end;
            }
        }

        result.add(new ReadInterval(first.chr, mergedStart, mergedEnd));

        return result.toArray(new ReadInterval[0]);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReadPair rp)) return false;
        if (!this.first.chr.equals(rp.first.chr)) return false;
        if (this.strand != rp.strand) return false;
//        this.getMergedAlignments();
//        rp.getMergedAlignments();
        ReadInterval[] merged1 = this.mergedAlignments;
        ReadInterval[] merged2 = rp.mergedAlignments;
        if (merged1.length != merged2.length) return false;
        for (int i = 0; i < merged1.length; i++) {
            if (! merged1[i].equals(merged2[i])) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
//        if (cachedHashCode == 0) {
//            getMergedAlignments();
//            int result = first.chr.hashCode();
//            result = 31 * result + (int) this.strand;
//            result = 31 * result + Arrays.hashCode(mergedAlignments);
//            cachedHashCode = result;
//        }
        return cachedHashCode;
    }
}
