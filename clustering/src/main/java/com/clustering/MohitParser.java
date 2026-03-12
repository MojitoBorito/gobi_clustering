package com.clustering;

import com.model.Element;
import com.model.UmiRead;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MohitParser implements Iterable<Element<UmiRead>>, Iterator<Element<UmiRead>>, Closeable {

    private final BufferedReader reader;
    private String nextLine;
    private boolean finished = false;
    private boolean iteratorTaken = false;
    private int elementCount = 0;

    public MohitParser(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);

        // skip header
        String header = reader.readLine();
        if (header == null) {
            finished = true;
            nextLine = null;
            return;
        }

        advance();
    }


    private void advance() {
        try {
            nextLine = reader.readLine();
            if (nextLine == null) {
                finished = true;
                reader.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return !finished;
    }

    @Override
    public Element<UmiRead> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        String line = nextLine;
        advance();

        String[] fields = line.split("\t", -1);

        String id = fields[0];
        String umi = fields[1];
        String seq = fields[2];
        UmiRead read = new UmiRead(umi, seq);

        return new Element<>(id, read);
    }

    @Override
    public Iterator<Element<UmiRead>> iterator() {
        if (iteratorTaken) {
            throw new IllegalStateException("This reader supports only one iterator");
        }
        iteratorTaken = true;
        return this;
    }

    @Override
    public void close() throws IOException {
        finished = true;
        reader.close();
    }
}