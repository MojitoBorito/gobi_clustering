package com.model;

public class Element<V> {
    private final String id;
    private final V value;

    public Element(String id, V value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public V getValue() {
        return value;
    }
}
