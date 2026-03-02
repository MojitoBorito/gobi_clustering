package com.filter;

public class UMI50BP {
    byte[] umi;
    byte[] seq;

    public UMI50BP(byte[] umi, byte[] seq) {
        this.umi = umi;
        this.seq = seq;
    }

    @Override
    public int hashCode() {
        return 31 * SeqKey.fnv1a(umi) + SeqKey.fnv1a(seq);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof UMI50BP other) {
            if (this.umi.length != other.umi.length) return false;
            if (this.seq.length != other.seq.length) return false;
            for (int i = 0; i < umi.length; i++) {
                if (this.umi[i] != other.umi[i]) return false;
            }
            for (int i = 0; i < seq.length; i++) {
                if (this.seq[i] != other.seq[i]) return false;
            }
            return true;
        }
        return false;
    }
}
