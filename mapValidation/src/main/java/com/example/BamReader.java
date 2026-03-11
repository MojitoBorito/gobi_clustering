package com.example;

import htsjdk.samtools.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BamReader {
    SamReaderFactory factory;
    String path;
    HashMap<String, Read> foundMate;
    HashMap<ReadPair, Set<String>> pcrCluster;
    HashSet<String> unmapped;

    public BamReader(String path) {
        this.path = path;
        factory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
        foundMate = new HashMap<>();
        pcrCluster = new HashMap<>();
        unmapped = new HashSet<>();

        readBam();

        System.out.println("bam.pcrCluster: "+pcrCluster.size());
        System.out.println("bam.unmapped: "+unmapped.size());
    }

    public void readBam(){
        try(SamReader reader = factory.open(new File(path))){
            Read mate;
            for(SAMRecord rec : reader){
                if ((mate = foundMate.get(rec.getReadName()))!=null){
                    if (checkReadUseless(rec)){
                        continue;
                    }
                    if (rec.getReadUnmappedFlag() || rec.getMateUnmappedFlag()){
                        unmapped.add(rec.getReadName());
                        continue;
                    }

                    Read first = mate.isFirstOfPair ? mate : processInterval(rec);
                    Read second =  !mate.isFirstOfPair ? mate : processInterval(rec);
                    ReadPair pair = new ReadPair(first, second);

                    pcrCluster.computeIfAbsent(pair, _ -> new HashSet<>()).add(rec.getReadName());

                    foundMate.remove(rec.getReadName());

                } else {
                    if (checkReadUseless(rec)){
                        continue;
                    }
                    if (rec.getReadUnmappedFlag() || rec.getMateUnmappedFlag()){
                        unmapped.add(rec.getReadName());
                    } else {
                        foundMate.put(rec.getReadName(), processInterval(rec));
                    }
                }
            }
        }catch (Exception e){
            System.err.println("Error opening SAM file: " + e.getMessage());
        }
    }

    public boolean checkReadUseless(SAMRecord record){
        if (!record.getReadPairedFlag()){
            return true;
        }
        if (record.isSecondaryOrSupplementary()){
            return true;
        }
        return false;
    }

    public Read processInterval(SAMRecord record){
        String chr = record.getReferenceName();
        int start = record.getAlignmentStart();
        int end = record.getAlignmentEnd();
        boolean isFirst = record.getFirstOfPairFlag();
        char strand = '.';
        Integer nm = record.getIntegerAttribute("NM");
        nm = (nm != null) ? nm : record.getIntegerAttribute("nM");
        nm = (nm != null) ? nm : record.getIntegerAttribute("XM");
        int mm = (nm != null) ? nm : 0;
        int clippingSize = (start - record.getUnclippedStart())
                + (record.getUnclippedEnd() - end);
        List<AlignmentBlock> blocks = record.getAlignmentBlocks();
        int splitCount = blocks.size()-1;
        return new Read(chr, start, end, strand, isFirst, mm, clippingSize, splitCount,
                blocks, record.getReadName());
    }
}
