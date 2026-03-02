package com.filter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

public class HashReadsIterator implements Iterator<HashReads>, Closeable {
    private BufferedReader reader;
    private HashReads nextRead;
    private boolean finished = false;

    public HashReadsIterator(String filename) throws IOException {
        InputStream fileStream = new FileInputStream(filename);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
        this.reader = new BufferedReader(decoder);
        advance(); // Load the first read
    }

    private void advance() {
        if (finished) return;

        try {
            String line;
            String header = null;
            String sequence = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("+") && header != null && sequence != null) {
                    continue;
                }

                if (header == null && line.startsWith("@")) {
                    int spaceIdx = line.indexOf(' ');
                    header = (spaceIdx == -1)
                            ? line.substring(1)
                            : line.substring(1, spaceIdx);
                    continue;
                }

                if (header != null && sequence == null) {
                    sequence = line;
                    continue;
                }

                if (header != null && sequence != null) {
                    String phred = line;
                    nextRead = new HashReads(
                            header,
                            sequence,
                            phred.getBytes(StandardCharsets.US_ASCII)
                    );
                    return;
                }
            }

            finished = true;
            nextRead = null;

        } catch (IOException e) {
            finished = true;
            nextRead = null;
            throw new UncheckedIOException("Error reading FASTQ file", e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public boolean hasNext() {
        return !finished && nextRead != null;
    }

    @Override
    public HashReads next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more reads in the FASTQ file");
        }
        HashReads current = nextRead;
        advance(); // pre-fetch the next record
        return current;
    }
}
