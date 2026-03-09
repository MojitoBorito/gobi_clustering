package com.clustering;

import com.model.Element;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MohitParser implements Iterable<MohitParser.Record>, Iterator<MohitParser.Record>, Closeable {

    public static class Record extends Element<String> {
        private final String umi;
        private final String seq;
        private final int counts;

        public Record(String umi, String seq, int counts) {
            super(umi, seq);
            this.umi = umi;
            this.seq = seq;
            this.counts = counts;
        }

        @Override
        public String toString() {
            return "Record{umi='" + umi + "', seq='" + seq + "', counts=" + counts + "}";
        }
    }

    private final BufferedReader reader;
    private String nextLine;
    private boolean finished = false;
    private boolean iteratorTaken = false;

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
    public Record next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        String line = nextLine;
        advance();

        String[] fields = line.split("\t", -1);
        if (fields.length != 3) {
            throw new IllegalArgumentException("Expected 3 tab-separated columns, got " + fields.length + " in line: " + line);
        }

        String umi = fields[0];
        String seq = fields[1];
        int counts = Integer.parseInt(fields[2]);

        return new Record(umi, seq, counts);
    }

    @Override
    public Iterator<Record> iterator() {
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