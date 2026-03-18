package FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class CountsReader {

    // Get counts for each transcript
    public static HashMap<String, Integer> getCounts(String file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            HashMap<String, Integer> transcriptCounts = new HashMap<>();
            String currentLine = reader.readLine();
            currentLine = reader.readLine(); // skip header
            while (currentLine != null) {
                String[] words = currentLine.split("\t");
                // gene_id, transcript_id, counts
                transcriptCounts.put(words[1], Integer.parseInt(words[2]));
                currentLine = reader.readLine();
            }
            return transcriptCounts;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
