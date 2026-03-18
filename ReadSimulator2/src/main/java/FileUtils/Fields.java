package FileUtils;


public enum Fields {
    SEQNAME(0),
    SOURCE(1),
    FEATURE(2),
    START(3),
    END(4),
    SCORE(5),
    STRAND(6),
    FRAME(7),
    ATTRIBUTES(8);

    final int index;

    Fields (int index) {
        this.index = index;
    }
}

