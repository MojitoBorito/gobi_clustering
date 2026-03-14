#!/usr/bin/env python3

from collections import defaultdict
import argparse
import os

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


from collections import defaultdict


def find_fp_examples(true_clusters, pred_clusters, max_examples=10):

    # read -> true cluster
    true_of_read = {}
    for cid, reads in true_clusters.items():
        for r in reads:
            true_of_read[r] = cid

    examples = []

    for pred_cluster_id, reads in pred_clusters.items():

        groups = defaultdict(list)

        for r in reads:
            true_c = true_of_read.get(r)
            groups[true_c].append(r)

        # if reads come from multiple true clusters → FP exists
        true_clusters_present = list(groups.values())

        if len(true_clusters_present) >= 2:

            g1 = true_clusters_present[0]
            g2 = true_clusters_present[1]

            examples.append((g1[0], g2[0]))

            if len(examples) >= max_examples:
                break

    return examples

def compute_stats(true_clusters, pred_clusters):
    true_sets = list(true_clusters.values())
    pred_sets = list(pred_clusters.values())

    # True positives/ true negatives
    count = 0
    tp = 0
    for p in pred_sets:
        for t in true_sets:

            tp += nC2(len(p & t))
            count += 1
    


    pred_pairs = sum(nC2(len(p)) for p in pred_sets)
    true_pairs = sum(nC2(len(t)) for t in true_sets)
    

    #Errors
    fp = pred_pairs - tp
    fn = true_pairs - tp

    #Stats
    precision = tp / (tp + fp) if (tp + fp) else 0
    recall = tp / (tp + fn) if (tp + fn) else 0

    
    return tp, fp, fn, precision, recall

def write_problematic_clusters(path: str, clusters):
    with open(path, "w") as f:
        for cluster_id, reads in clusters.items():
            for read_id in reads:
                f.write(f"{cluster_id}\t{read_id}\n")
        

def main():
    parser = argparse.ArgumentParser(
        description="Evaluate clustering against ground truth"
    )

    parser.add_argument("-model", help="Model cluster file")
    parser.add_argument("-predicted", help="Predicted cluster file")
    parser.add_argument("-o", help="Directory to write out problematic clusters")

    args = parser.parse_args()

    true_clusters = get_clusters_dict(args.model)
    pred_clusters = get_clusters_dict(args.predicted)

    tp, fp, fn, precision, recall = compute_stats(true_clusters, pred_clusters)

    print("Clustering evaluation")
    print("---------------------")
    print(f"True Positives : {tp}")
    print(f"False Positives: {fp}")
    print(f"False Negatives: {fn}")
    print()
    print(f"Precision: {precision:.4f}")
    print(f"Recall   : {recall:.4f}")

    print('False positives sample:')
    print(find_fp_examples(true_clusters, pred_clusters, 10))
    print('False negatives sample:')
    print(find_fp_examples(pred_clusters, true_clusters, 10))



if __name__ == "__main__":
    main()
