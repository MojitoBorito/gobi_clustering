public class Cluster<T> {
    private final int id;
    private final ArrayList<T> items;
    private final DistanceMetric<T> distanceMetric;

    public Bucket(int id, DistanceMetric<T> distanceMetric) {
        this.id = id;
        this.distanceMetric = distanceMetric;
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    
    public void addItem(T item) {
        items.add(item);
    }

    public ArrayList<T> getItems() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public DistanceMetric<T> geDistanceMetric() {
        return distanceMetric;
    }

}