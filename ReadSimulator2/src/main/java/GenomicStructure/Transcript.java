package GenomicStructure;

import java.util.List;

public class Transcript {
    private final RegionVector exons;
    private final String transcriptID;
    private int start = -1;
    private int end = -1;
    private final boolean fwStrand;

    public Transcript(String transcriptID, boolean fwStrand) {
        this.transcriptID = transcriptID;
        this.exons = new RegionVector(!fwStrand);
        this.fwStrand = fwStrand;
    }

    public void addExon(Region e) {
        exons.addRegion(e);
    }

    // Sort exons
    // Then, for each exon create genomic coordinates -> transcript coordinates association
    public void setUpTranscript() {
        exons.setUpRegions();
        this.start = exons.getStart();
        this.end = exons.getEnd();
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public boolean isFwStrand() {
        return fwStrand;
    }

    public List<Region> getExons() {
        return exons.getRegions();
    }

    public RegionVector getRegionVector() {
        return exons;
    }

    public String getId() {
        return transcriptID;
    }
}
