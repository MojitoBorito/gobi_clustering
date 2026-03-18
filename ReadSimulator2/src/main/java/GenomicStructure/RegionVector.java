package GenomicStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegionVector {
    private final List<Region> regions;
    private boolean inverseVector;

    public void addRegion(Region region) {
        regions.add(region);
    }

    public List<Region> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    public RegionVector(boolean inverseVector) {
        this.inverseVector = inverseVector;
        this.regions = new ArrayList<>();
    }

    // Assumed that regions are sorted accordingly
    public RegionVector(boolean inverseVector, ArrayList<Region> regions) {
        this.inverseVector = inverseVector;
        this.regions = new ArrayList<>(regions);
    }


    public void setUpRegions() {
        if (!inverseVector) {
            Collections.sort(regions);
        } else {
            regions.sort(Collections.reverseOrder());
        }
        assignRelativeCoords();
    }

    private void assignRelativeCoords() {
        int totalLength = 0;
        for (Region region : regions) {
            region.setRelativeStart(totalLength);
            totalLength += region.getAbsoluteEnd() - region.getAbsoluteStart() + 1;
            region.setRelativeEnd(totalLength);
        }
    }

    public RegionVector reversedCopy() {
        ArrayList<Region> reversedRegions = new ArrayList<>(regions.size());

        // reverse order, deep copy regions
        for (int i = regions.size() - 1; i >= 0; i--) {
            Region r = regions.get(i);
            reversedRegions.add(r.copy());
        }

        // inverseVector flipped
        RegionVector reversed = new RegionVector(!inverseVector, reversedRegions);
        reversed.assignRelativeCoords(); // recompute relativeStart/End for reversed orientation
        return reversed;
    }

    public RegionVector copy() {
        ArrayList<Region> copiedRegions = new ArrayList<>(regions.size());

        for (Region region : regions) {
            copiedRegions.add(region.copy());
        }

        return new RegionVector(inverseVector, copiedRegions);
    }

    public int getStart() {
        return inverseVector ? regions.getLast().getAbsoluteStart() : regions.getFirst().getAbsoluteStart();
    }

    public int getEnd() {
        return inverseVector ? regions.getFirst().getAbsoluteEnd() : regions.getLast().getAbsoluteEnd();
    }



    // Assuming regions are sorted
    public RegionVector subset(int relativeStart, int relativeEnd) {
        if (relativeStart < 0 || relativeEnd <= relativeStart) {
            throw new IllegalArgumentException("Invalid range: [" + relativeStart + ", " + relativeEnd + ")");
        }
        if (regions.isEmpty()) {
            throw new IllegalStateException("RegionVector is empty");
        }

        ArrayList<Region> out = new ArrayList<>();

        // 1) find index of region containing relativeStart
        int current = 0;
        while (current < regions.size() &&
                relativeStart >= regions.get(current).getRelativeEnd()) {
            current++;
        }
        if (current == regions.size()) {
            throw new IllegalArgumentException("relativeStart out of bounds: " + relativeStart);
        }

        Region startRegion = regions.get(current);

        // 2) construct cropped start region
        int newAbsStart = startRegion.getAbsoluteStart();
        int newAbsEnd   = startRegion.getAbsoluteEnd();

        int offset = relativeStart - startRegion.getRelativeStart(); // 0-based offset into this region
        if (!inverseVector) {
            newAbsStart = startRegion.getAbsoluteStart() + offset;
        } else {
            newAbsEnd = startRegion.getAbsoluteEnd() - offset;
        }

        // Singleton
        if (relativeEnd <= startRegion.getRelativeEnd()) {
            if (!inverseVector) {
                int offsetToLastIncluded = relativeEnd - 1 - startRegion.getRelativeStart();
                newAbsEnd = startRegion.getAbsoluteStart() + offsetToLastIncluded;
            } else {
                int offsetFromLeft = startRegion.getRelativeEnd() - relativeEnd;
                newAbsStart = startRegion.getAbsoluteStart() + offsetFromLeft;
            }
            out.add(new Region(newAbsStart, newAbsEnd));
            RegionVector result = new RegionVector(inverseVector, out);
            result.setUpRegions();
            return result;
        }


        Region newStart = new Region(newAbsStart, newAbsEnd);
        out.add(newStart);

        // 3) add full middle regions
        current++;
        while (current < regions.size() &&
                relativeEnd > regions.get(current).getRelativeEnd()) {
            out.add(regions.get(current).copy());
            current++;
        }

        // 4) handle end region (may be same as start, or a different one)
        if (current < regions.size()) {
            Region endRegion = regions.get(current);

            newAbsStart = endRegion.getAbsoluteStart();
            newAbsEnd   = endRegion.getAbsoluteEnd();

            if (!inverseVector) {
                int offsetToLastIncluded = relativeEnd - 1 - endRegion.getRelativeStart();
                newAbsEnd = endRegion.getAbsoluteStart() + offsetToLastIncluded;
            } else {
                int offsetFromLeft = endRegion.getRelativeEnd() - relativeEnd;
                newAbsStart = endRegion.getAbsoluteStart() + offsetFromLeft;
            }

            Region newEnd = new Region(newAbsStart, newAbsEnd);
            if (!newEnd.equals(newStart)) { // avoid duplicate if single-region case
                out.add(newEnd);
            }
        }

        RegionVector result = new RegionVector(inverseVector, out);
        result.setUpRegions(); // recompute relative coordinates for the cropped vector
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Region region : regions) {
            sb.append(region).append("|");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static void main(String[] args) {
        RegionVector rv = new RegionVector(true); // false = positive strand
        rv.addRegion(new Region(41196312, 41197819));
        rv.addRegion(new Region(41199660, 41199720));
    rv.addRegion(new Region(41201138, 41201211));
        rv.addRegion(new Region(41203080, 41203134));
        rv.addRegion(new Region(41209069, 41209152));
        rv.addRegion(new Region(41215350, 41215390));
        rv.addRegion(new Region(41215891, 41215968));
        rv.addRegion(new Region(41219625, 41219712));
        rv.addRegion(new Region(41222945, 41223255));
        rv.addRegion(new Region(41226348, 41226538));
        rv.addRegion(new Region(41228505, 41228631));
        rv.addRegion(new Region(41234421, 41234592));
        rv.addRegion(new Region(41242961, 41243049));
        rv.addRegion(new Region(41243452, 41246877));
        rv.addRegion(new Region(41247863, 41247939));
        rv.addRegion(new Region(41249261, 41249306));
        rv.addRegion(new Region(41251792, 41251897));
        rv.addRegion(new Region(41256139, 41256278));
        rv.addRegion(new Region(41256885, 41256973));
        rv.addRegion(new Region(41258473, 41258550));
        rv.addRegion(new Region(41267743, 41267796));
        rv.addRegion(new Region(41276034, 41276132));
        rv.addRegion(new Region(41277288, 41277387));

// then sort & assign transcript-relative coordinates
        rv.setUpRegions();

        RegionVector cropped = rv.subset( 2459,2534);
        System.out.println(cropped);
    }

}
