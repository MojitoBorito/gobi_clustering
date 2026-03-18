package GenomicStructure;

import java.util.HashMap;

public class Gene {
    private final String chr;
    private final String id;
    private final HashMap<String, Transcript> transcripts;
    private final char strand;

    public Gene(String chr, String id, char strand) {
        this.chr = chr;
        this.id = id;
        this.strand = strand;
        transcripts = new HashMap<>();
    }

    public char getStrand() {
        return strand;
    }

    public HashMap<String, Transcript> getTranscripts() {
        return transcripts;
    }

    public boolean isPositiveStrand() {
        return strand == '+';
    }

    public String getChr() {
        return chr;
    }

    public String getId() {
        return id;
    }


}
