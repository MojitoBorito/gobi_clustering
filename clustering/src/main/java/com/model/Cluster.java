package com.model;

import java.util.ArrayList;

public class Cluster<K, E> {
    private final K id;
    private final ArrayList<E> elements;


    public Cluster(K id) {
        this.id = id;
        this.elements = new ArrayList<>();
    }

    public K getId() {
        return id;
    }
    
    public void addElement(E elem) {
        elements.add(elem);
    }

    public ArrayList<E> getElements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}