package GenomicStructure;


public class Region implements Comparable<Region> {

    // Start/end in genome coordinates
    // 1-based, end inclusive
    private final int absoluteStart;
    private final int absoluteEnd;

    // Start/end in transcript coordinates
    // 0-based, end exclusive
    private int relativeStart = Integer.MIN_VALUE;
    private int relativeEnd = Integer.MIN_VALUE;

    public Region(int absoluteStart, int absoluteEnd) {
        if (absoluteStart > absoluteEnd) throw new RuntimeException("Illegal region created");
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
    }

    public Region(int absoluteStart, int absoluteEnd, int relativeStart, int relativeEnd) {
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
        this.relativeStart = relativeStart;
        this.relativeEnd = relativeEnd;
    }

    @Override
    public int compareTo(Region e) {
        return Integer.compare(absoluteStart, e.absoluteStart);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region other)) return false;
        return absoluteStart == other.absoluteStart &&
                absoluteEnd == other.absoluteEnd;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(absoluteStart);
        result = 31 * result + Integer.hashCode(absoluteEnd);
        return result;
    }


    // Print in 1-based, end exclusive
    @Override
    public String toString() {
        return absoluteStart + "-" + (absoluteEnd+1);
    }


    public int getRelativeStart() {
        return relativeStart;
    }

    public int getRelativeEnd() {
        return relativeEnd;
    }

    public void setRelativeStart(int relativeStart) {
        this.relativeStart = relativeStart;
    }

    public void setRelativeEnd(int relativeEnd) {
        this.relativeEnd = relativeEnd;
    }

    public int getAbsoluteStart() {
        return absoluteStart;
    }

    public int getAbsoluteEnd() {
        return absoluteEnd;
    }


    public int size() {
        return absoluteEnd - absoluteStart +1;
    }

    public Region copy() {
        return new Region(absoluteStart, absoluteEnd, relativeStart, relativeEnd);
    }

}
