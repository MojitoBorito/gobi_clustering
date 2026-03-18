package FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FastaReader {
    private final String path;
    private final RandomAccessFile raf;
    private final HashMap<String, SequenceInfo> index;

    public FastaReader(String fastaPath, String fastaIndex) throws FileNotFoundException {
        this.path = fastaPath;
        this.raf = new RandomAccessFile(path, "r");
        this.index = readIndex(fastaIndex);
    }

    // In fasta file
    private String getSequence(long offset, int bufferSize) throws IOException {
        raf.seek(offset);
        byte[] buffer = new byte[bufferSize];
        raf.readFully(buffer);
        return(new String(buffer, StandardCharsets.UTF_8).replace("\n", ""));
    }

    // start/end inclusive
    public String getSequence(String seqName, int start, int end) throws IOException {
        SequenceInfo seq = index.get(seqName);
        long totalOffsetStart = seq.calculateTotalOffset(start);
        long totalOffsetEnd = seq.calculateTotalOffset(end);
        return getSequence(totalOffsetStart, (int) (totalOffsetEnd-totalOffsetStart+1));
    }

    private HashMap<String, SequenceInfo> readIndex(String fastaIndex) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fastaIndex));
            HashMap<String, SequenceInfo> index = new HashMap<>();
            String[] words;
            String currentLine = reader.readLine();
            while (currentLine != null) {
                words = currentLine.split("\t");
                String seqName = words[IndexFields.SEQ_NAME.index];
                long startOffset = Long.parseLong(words[IndexFields.START_OFFSET.index]);
                int seqLength = Integer.parseInt(words[IndexFields.SEQ_LENGTH.index]);
                int basesPerLine = Integer.parseInt(words[IndexFields.BASES_PER_LINE.index]);
                int bytesPerLine = Integer.parseInt(words[IndexFields.BYTES_PER_LINE.index]);
                index.put(seqName, new SequenceInfo(startOffset, seqLength, basesPerLine, bytesPerLine));
                currentLine = reader.readLine();
            }
            return index;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: java -jar FastaReader.jar <fai_file> <fasta_file> <seq_name> <start> <end>");
            System.exit(1);
        }

        String faiPath = args[0];
        String fastaPath = args[1];
        String seqName = args[2];
        int start = Integer.parseInt(args[3]);
        int end = Integer.parseInt(args[4]);

        try {
            FastaReader reader = new FastaReader(fastaPath, faiPath);
            String sequence = reader.getSequence(seqName, start, end);
            System.out.println(">" + seqName + ":" + start + "-" + end);
            System.out.println(sequence);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
