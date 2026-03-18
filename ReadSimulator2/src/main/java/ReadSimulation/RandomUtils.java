package ReadSimulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RandomUtils {


    public static List<Integer> generateDistinct_HashSet(Random random, int n, int range) {
        HashSet<Integer> ints = new HashSet<>();
        while(ints.size() < n) {
            ints.add(random.nextInt(range));
        }
        return new ArrayList<>(ints);
    }

    public static List<Integer> generateDistinct_Shuffle(Random random, int n, int range) {
        List<Integer> ints = new ArrayList<>(range);
        for (int i = 0; i < range; i++) ints.add(i);
        Collections.shuffle(ints, random);
        return ints.subList(0, n);
    }

    // Knuth's algorithm for Poisson sampling
    public static int samplePoisson(Random random, double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= random.nextDouble();
        } while (p > L);

        return k - 1;
    }


    public static void compare(int range, int reps) throws IOException {
        Random rand = new Random();
        try (FileWriter fw = new FileWriter("timings.csv")) {
            fw.write("n,range,method,time_ms\n");

            for (int n = 1; n <= range; n += 10) {
                long start = 0, end = 0, avg1 = 0, avg2 = 0;

                for (int i = 0; i < reps; i++) {
                    start = System.nanoTime();
                    generateDistinct_HashSet(rand, n, range);
                    end = System.nanoTime();
                    avg1 += end-start;

                    start = System.nanoTime();
                    generateDistinct_Shuffle(rand, n, range);
                    end = System.nanoTime();
                    avg2 += end - start;
                }
                fw.write(n + "," + range + ",HashSet," + (avg1/reps) / 1e6 + "\n");
                fw.write(n + "," + range + ",Shuffle," + (avg2/reps) / 1e6 + "\n");

            }
        }
    }

    public static void main(String[] args) throws IOException {
        compare(1000, 1000);
    }
}
