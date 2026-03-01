package com.filter;

import java.io.*;
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
                    header = line.substring(1).split(" ")[0];
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
                            sequence.getBytes(StandardCharsets.US_ASCII),
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
