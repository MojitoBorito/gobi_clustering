package ReadSimulation;

import java.util.ArrayList;
import java.util.Random;

public class UmiFactory {
    char[] bases = {'A', 'C', 'G', 'T'};
    private final Random rand;
    private ArrayList<String> umis;
    private final int numOfUmis;

    public UmiFactory(Random random, int length, int numOfUmis) {
        this.rand = random;
        this.numOfUmis = numOfUmis;
        this.umis = new ArrayList<>(numOfUmis);
        for (int i = 0; i < numOfUmis; i++) {
            char[] umi = new char[length];
            for (int j = 0; j < length; j++) {
                umi[j] = bases[rand.nextInt(4)];
            }
            umis.add(new String(umi));
        }
    }

    public String getUmi() {
        return umis.get(rand.nextInt(0, numOfUmis));
    }
}
