package FileUtils;

public record SequenceInfo(long startOffset, int seqLength,
                           int basesPerLine, int bytesPerLine) {

    // Start position is given in bases from start of chromosome
    // Start position must be 1 based (as GTF files are)
    // Total offset is 0 based
    // startOffset points to the first byte of the respective sequence (inclusive and 0 based)
    public long calculateTotalOffset(int startPosition) {
        return startOffset
                + (startPosition-1)
                + (long) (bytesPerLine - basesPerLine)
                * ((startPosition-1) / basesPerLine);
    }
}
