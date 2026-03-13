#!/usr/bin/env python3

import argparse
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.metrics import adjusted_rand_score
from sklearn.metrics import homogeneity_completeness_v_measure
from sklearn.metrics.cluster import contingency_matrix


def parse_args():
    parser = argparse.ArgumentParser(
        description="Evaluate predicted clustering against ground truth."
    )
    parser.add_argument(
        "--true_clusters",
        required=True,
        help="Path to TSV file with ground-truth clusters (columns: cluster_id, read_id)"
    )
    parser.add_argument(
        "--predicted_clusters",
        required=True,
        help="Path to TSV file with predicted clusters (columns: cluster_id, read_id)"
    )
    parser.add_argument(
        "--output",
        required=True,
        help="Path to output text file"
    )
    return parser.parse_args()


def comb2(x):
    return x * (x - 1) / 2


def compute_ari(true_labels, predicted_labels):
    return adjusted_rand_score(true_labels, predicted_labels)


def pairwise_precision_recall_f1(true_labels, pred_labels):
    C = contingency_matrix(true_labels, pred_labels, sparse=True)

    # True positives: pairs that are together in both true and predicted clustering
    tp = comb2(C.data).sum()

    # Number of predicted positive pairs
    col_sums = np.asarray(C.sum(axis=0)).ravel()
    pred_pairs = comb2(col_sums).sum()

    # Number of true positive pairs
    row_sums = np.asarray(C.sum(axis=1)).ravel()
    true_pairs = comb2(row_sums).sum()

    precision = tp / pred_pairs if pred_pairs > 0 else 0.0
    recall = tp / true_pairs if true_pairs > 0 else 0.0
    f1 = (
        2 * precision * recall / (precision + recall)
        if precision + recall > 0 else 0.0
    )

    return precision, recall, f1


def compute_v(true_labels, predicted_labels):
    homogeneity, completeness, v_measure = homogeneity_completeness_v_measure(
        true_labels, predicted_labels
    )
    return homogeneity, completeness, v_measure


def load_and_merge(true_path, predicted_path):
    true_clusters = pd.read_csv(true_path, sep="\t")
    predicted_clusters = pd.read_csv(predicted_path, sep="\t")

    required_cols = {"cluster_id", "read_id"}

    if not required_cols.issubset(true_clusters.columns):
        raise ValueError(
            f"True clusters file must contain columns {required_cols}, "
            f"but has {set(true_clusters.columns)}"
        )

    if not required_cols.issubset(predicted_clusters.columns):
        raise ValueError(
            f"Predicted clusters file must contain columns {required_cols}, "
            f"but has {set(predicted_clusters.columns)}"
        )

    merged = true_clusters.merge(
        predicted_clusters,
        on="read_id",
        suffixes=("_true", "_pred")
    )

    if merged.empty:
        raise ValueError(
            "Merged dataset is empty. The two files do not appear to share any read_id values."
        )

    return true_clusters, predicted_clusters, merged


def write_report(
    output_path,
    true_clusters,
    predicted_clusters,
    merged,
    ari,
    precision,
    recall,
    f1,
    homogeneity,
    completeness,
    v_measure
):
    output_path = Path(output_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with output_path.open("w", encoding="utf-8") as out:
        out.write("Clustering Evaluation Report\n")
        out.write("============================\n\n")

        out.write("Input summary\n")
        out.write("-------------\n")
        out.write(f"True rows: {len(true_clusters)}\n")
        out.write(f"Predicted rows: {len(predicted_clusters)}\n")
        out.write(f"Merged rows: {len(merged)}\n")
        out.write(f"Unique true clusters: {merged['cluster_id_true'].nunique()}\n")
        out.write(f"Unique predicted clusters: {merged['cluster_id_pred'].nunique()}\n\n")

        out.write("Metrics\n")
        out.write("-------\n")
        out.write(f"Adjusted Rand Index (ARI): {ari:.10f}\n")
        out.write(f"Pairwise Precision:        {precision:.10f}\n")
        out.write(f"Pairwise Recall:           {recall:.10f}\n")
        out.write(f"Pairwise F1:               {f1:.10f}\n")
        out.write(f"Homogeneity:               {homogeneity:.10f}\n")
        out.write(f"Completeness:              {completeness:.10f}\n")
        out.write(f"V-measure:                 {v_measure:.10f}\n")


def main():
    args = parse_args()

    true_clusters, predicted_clusters, merged = load_and_merge(
        args.true_clusters,
        args.predicted_clusters
    )

    true_labels = merged["cluster_id_true"]
    predicted_labels = merged["cluster_id_pred"]

    ari = compute_ari(true_labels, predicted_labels)
    precision, recall, f1 = pairwise_precision_recall_f1(true_labels, predicted_labels)
    homogeneity, completeness, v_measure = compute_v(true_labels, predicted_labels)

    write_report(
        args.output,
        true_clusters,
        predicted_clusters,
        merged,
        ari,
        precision,
        recall,
        f1,
        homogeneity,
        completeness,
        v_measure
    )

    print(f"Evaluation written to: {args.output}")


if __name__ == "__main__":
    main()