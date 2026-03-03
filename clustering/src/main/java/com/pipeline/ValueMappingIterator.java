package com.pipeline;

import com.model.Element;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public final class ValueMappingIterator<InV, OutV> implements Iterator<Element<OutV>> {
    private final Iterator<? extends Element<InV>> in;
    private final Function<InV, ? extends OutV> map;

    public ValueMappingIterator(Iterator<? extends Element<InV>> in,
                                Function<InV, ? extends  OutV> map) {

        this.in = Objects.requireNonNull(in);
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public boolean hasNext() {
        return in.hasNext();
    }

    @Override
    public Element<OutV> next() {
        Element<InV> e = in.next();
        OutV out = map.apply(e.getValue());
        return new Element<>(e.getId(), out);
    }
}
