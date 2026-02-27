package com.example;

public class SeqKey {
    final byte[] source;
    final int offset;
    final int length;
    final long hash;

    public SeqKey(byte[] seq, int last) {
        this.source = seq;
        this.offset = seq.length - last;
        this.length = last;
        this.hash = computeHash();
    }

    private long computeHash() {
        long h = 0xcbf29ce484222325L;
        for (int i = offset; i < offset + length; i++) {
            h ^= source[i];
            h *= 0x100000001b3L;
        }
        return h;
    }

    @Override
    public int hashCode() {
        return (int) (hash ^ (hash >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SeqKey other) {
            if (this.length != other.length) return false;
            for (int i = 0; i < length; i++) {
                if (source[offset + i] != other.source[other.offset + i]) return false;
            }
            return true;
        }
        return false;
    }

    public static int fnv1a(byte[] data) {
        int hash = 0x811c9dc5;
        for (int i = 0; i < data.length; i++) {
            hash ^= data[i];
            hash *= 0x01000193;
        }
        return hash;
    }

    public static long packSequence(byte[] seq) {
        long packed = 0;
        for (byte b : seq) {
            long bits = switch (b) {
                case 'A', 'a' -> 0L;
                case 'C', 'c' -> 1L;
                case 'G', 'g' -> 2L;
                case 'T', 't' -> 3L;
                default -> 4L;  // N
            };
            packed = (packed << 3) | bits;
        }
        return packed;
    }
}
