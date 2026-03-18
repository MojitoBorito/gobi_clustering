package FileUtils;

import GenomicStructure.Sequence;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;

public class OutputUtils {




    public static void recreateDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            // Recursively delete contents
            try (var walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder())  // delete children before parents
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete " + path, e);
                            }
                        });
            }
        }
        // Recreate empty directory
        Files.createDirectories(dir);
    }

}
