public interface DistanceMetric<T> {
    // Must be symmetric
    double compute(T t1, T t2);
}