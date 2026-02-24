public class SingleLinkage<T> implements ClusterDistance<T>{
    @Override
    public double distanceToCluster(T elem, Cluster<T> cluster) {
        double minDistance = Double.POSITIVE_INFINITY;
        for (T item : items) {
            minDistance = Math.min(minDistance, cluster.geDistanceMetric.compute(elem, item))
        }
        return minDistance;
    }
}
