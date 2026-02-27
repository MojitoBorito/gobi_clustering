package com.model;

import java.util.ArrayList;

public class Cluster<E> {
    private final int id;
    private final ArrayList<E> elements;


    public Cluster(int id) {
        this.id = id;
        this.elements = new ArrayList<>();
    }

    public int getId() {
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

    @Override
    public String toString() {
        return "Cluster: " + id + " of size: " + size();
    }
}