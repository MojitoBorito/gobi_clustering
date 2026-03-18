package FileUtils;

public enum IndexFields {
        SEQ_NAME(0),
        SEQ_LENGTH(1),
        START_OFFSET(2), // In bytes
        BASES_PER_LINE(3),
        BYTES_PER_LINE(4);
        final int index;

        IndexFields (int index) {
            this.index = index;
        }
}
