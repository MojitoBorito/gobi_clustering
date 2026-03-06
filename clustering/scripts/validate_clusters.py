#!/usr/bin/env python3

from collections import defaultdict
import argparse


def get_clusters_dict(path: str):
    clusters = defaultdict(set)

    with open(path) as f:
        next(f)  # skip header
        for line in f:
            if not line.strip():
                continue

            cluster_id, read_id = line.strip().split("\t")
            clusters[int(cluster_id)].add(int(read_id))

    return clusters


def nC2(n):
    return n * (n - 1) // 2


def compute_stats(true_clusters, pred_clusters):
    true_sets = list(true_clusters.values())
    pred_sets = list(pred_clusters.values())

    # True positives
    tp = 0
    for p in pred_sets:
        for t in true_sets:
            tp += nC2(len(p & t))

    pred_pairs = sum(nC2(len(p)) for p in pred_sets)
    true_pairs = sum(nC2(len(t)) for t in true_sets)

    fp = pred_pairs - tp
    fn = true_pairs - tp

    precision = tp / (tp + fp) if (tp + fp) else 0
    recall = tp / (tp + fn) if (tp + fn) else 0

    return tp, fp, fn, precision, recall


def main():
    parser = argparse.ArgumentParser(
        description="Evaluate clustering against ground truth"
    )

    parser.add_argument("model", help="Model cluster file")
    parser.add_argument("predicted", help="Predicted cluster file")

    args = parser.parse_args()

    true_clusters = get_clusters_dict(args.model)
    pred_clusters = get_clusters_dict(args.predicted)

    tp, fp, fn, precision, recall, f1 = compute_stats(true_clusters, pred_clusters)

    print("Clustering evaluation")
    print("---------------------")
    print(f"True Positives : {tp}")
    print(f"False Positives: {fp}")
    print(f"False Negatives: {fn}")
    print()
    print(f"Precision: {precision:.4f}")
    print(f"Recall   : {recall:.4f}")


if __name__ == "__main__":
    main()