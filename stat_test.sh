#!/usr/bin/env bash
set -euo pipefail

# ─────────────────────────── Configuration ───────────────────────────
FASTQ_DIR="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq"
BLOCK_BASE="/mnt/biocluster/praktikum/genprakt/gruppe_g"
RESULTS_BASE_NAME="dual_clustering"
RESULTS_BASE="$BLOCK_BASE/$RESULTS_BASE_NAME"

JAR="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/clustering/target/clustering-jar-with-dependencies.jar"

mkdir -p "$RESULTS_BASE"

# ─────────────────────────── Main loop ───────────────────────────────
# Iterate over all UMI files (R2), excluding G1 samples.
# The glob H*-T2_R2_001.fastq.gz already excludes G1 (starts with G) and 'mapped' (a directory).
for UMI in "$FASTQ_DIR"/H*-T2_R2_001.fastq.gz; do

    fname=$(basename "$UMI")

    # Extract sample ID, e.g. "H1-12936-T2" from "H1-12936-T2_R2_001.fastq.gz"
    sample_id=${fname%%_R2_001.fastq.gz}

    # Build the companion read paths
    R1="$FASTQ_DIR/${sample_id}_R1_001.fastq.gz"   # forward reads
    R3="$FASTQ_DIR/${sample_id}_R3_001.fastq.gz"   # reverse reads

    # ── Create output directory tree ──
    #   <RESULTS_BASE>/<sample_id>/R1/   — results from forward-read run
    #   <RESULTS_BASE>/<sample_id>/R3/   — results from reverse-read run
    SAMPLE_DIR="$RESULTS_BASE/$sample_id"
    R1_OUT_DIR="$SAMPLE_DIR/R1"
    R3_OUT_DIR="$SAMPLE_DIR/R3"
    mkdir -p "$R1_OUT_DIR" "$R3_OUT_DIR"

    # ── Run for R1 (forward reads) ──
    echo "===== [$sample_id] Processing R1 (forward) ====="
    echo "  UMI:   $UMI"
    echo "  Reads: $R1"
    echo "  Out:   $R1_OUT_DIR"

    java -Xmx150g -jar "$JAR" \
        -umi   "$UMI" \
        -reads "$R1" \
        -outDir "$R1_OUT_DIR" \
        -kmer_size "30" \
        -threshold "0.03" \
        -read_length "150" \
        -umi_length "12" \
        2>&1 | tee "$R1_OUT_DIR/log.txt"

    # ── Run for R3 (reverse reads) ──
    echo "===== [$sample_id] Processing R3 (reverse) ====="
    echo "  UMI:   $UMI"
    echo "  Reads: $R3"
    echo "  Out:   $R3_OUT_DIR"

    java -Xmx150g -jar "$JAR" \
        -umi   "$UMI" \
        -reads "$R3" \
        -outDir "$R3_OUT_DIR" \
        -kmer_size "30" \
        -threshold "0.03" \
        -read_length "150" \
        -umi_length "12" \
        2>&1 | tee "$R3_OUT_DIR/log.txt"

    echo ""
done

echo "All samples processed. Results in: $RESULTS_BASE"
