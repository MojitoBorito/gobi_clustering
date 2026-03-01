package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

public class FastqIterator implements Iterator<Sequence>, AutoCloseable {

    private final BufferedReader reader;
    private Sequence nextSequence;

    public FastqIterator(String fileName) throws IOException {
        InputStream fileStream = new FileInputStream(fileName);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
        this.reader = new BufferedReader(decoder);

        advance(); // preload first sequence
    }

    private void advance() {
        try {
            String header = reader.readLine();
            if (header == null) {
                nextSequence = null;
                return;
            }

            String sequence = reader.readLine();
            reader.readLine(); // skip "+"
            String phred = reader.readLine();

            if (sequence == null || phred == null) {
                nextSequence = null;
                return;
            }

            nextSequence = new Sequence(
                    header.substring(1),
                    sequence.getBytes(StandardCharsets.US_ASCII),
                    phred.getBytes(StandardCharsets.US_ASCII)
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextSequence != null;
    }

    @Override
    public Sequence next() {
        if (nextSequence == null)
            throw new NoSuchElementException();

        Sequence current = nextSequence;
        advance();
        return current;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}