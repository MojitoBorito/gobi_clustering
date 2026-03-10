package com.encoding;

import com.bucket.UmiKey;
import com.model.Encoder;
import com.model.UmiRead;

public class UmiPosKmerBitEncoder implements Encoder<UmiRead, UmiKey> {
    private final PosKmerBitEncoder encoder;

    public UmiPosKmerBitEncoder(int w) {
        this.encoder = new PosKmerBitEncoder(w);
    }

    @Override
    public UmiKey encode(UmiRead value) {
        return new UmiKey(value.umi(), encoder.encode(value.sequence()));
    }
}
