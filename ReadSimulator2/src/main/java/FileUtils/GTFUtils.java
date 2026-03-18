package FileUtils;

import GenomicStructure.Gene;
import GenomicStructure.Region;
import GenomicStructure.Transcript;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class GTFUtils {

    public static HashMap<String, Gene> readGTF(String path, Set<String> transcriptFilter) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path), 65536)) {
            return readGTF(reader, transcriptFilter);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + path, e);
        }
    }

    public static HashMap<String, Gene> readGTFGz(String path, Set<String> transcriptFilter) {
        try (
                GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(path));
                InputStreamReader isr = new InputStreamReader(gzipStream);
                BufferedReader reader = new BufferedReader(isr, 65536)
        ) {
            return readGTF(reader, transcriptFilter);
        } catch (IOException e) {
            throw new RuntimeException("Could not read gzipped file: " + path, e);
        }
    }

    private static HashMap<String, Gene> readGTF(BufferedReader reader, Set<String> transcriptFilter) {
        try {
            String currentLine = reader.readLine();
            String[] wordsArray;
            HashMap<String, Gene> genes = new HashMap<>();

            while (currentLine != null) {
                if (currentLine.startsWith("#")) {
                    currentLine = reader.readLine();
                    continue;
                }

                wordsArray = currentLine.split("\t");
                if (wordsArray.length != 9) {
                    throw new RuntimeException("GTF line read incorrectly");
                }

                String chr = wordsArray[Fields.SEQNAME.index];
                String feature = wordsArray[Fields.FEATURE.index];
                char strand = wordsArray[Fields.STRAND.index].charAt(0);
                int start = Integer.parseInt(wordsArray[Fields.START.index]);
                int end = Integer.parseInt(wordsArray[Fields.END.index]);

                HashMap<String, String> attributes = new HashMap<>();
                for (String part : wordsArray[Fields.ATTRIBUTES.index].split(";")) {
                    part = part.trim();
                    if (part.isEmpty()) continue;

                    String[] kv = part.split(" ", 2);
                    if (kv.length < 2) continue;

                    attributes.put(kv[0], kv[1].replace("\"", "").trim());
                }

                String geneID = attributes.get("gene_id");
                String transcriptID = attributes.getOrDefault("transcript_id", "");

                if (geneID == null) {
                    currentLine = reader.readLine();
                    continue;
                }

                // Skip everything that is not in transcript filter
                if (!transcriptFilter.contains(transcriptID)) {
                    currentLine = reader.readLine();
                    continue;
                }

                Gene gene = genes.computeIfAbsent(geneID, id -> new Gene(chr, id, strand));

                if (!transcriptID.isEmpty()) {
                    Transcript tx = gene.getTranscripts()
                            .computeIfAbsent(transcriptID, id -> new Transcript(id, strand == '+'));

                    if (feature.equals("exon")) {
                        tx.addExon(new Region(start, end));
                    }
                }

                currentLine = reader.readLine();
            }

            genes.values().forEach(g ->
                    g.getTranscripts().values().forEach(Transcript::setUpTranscript)
            );

            return genes;
        } catch (IOException e) {
            throw new RuntimeException("Error while reading GTF", e);
        }
    }
}