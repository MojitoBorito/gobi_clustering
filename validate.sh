#!/usr/bin/env bash
set -euo pipefail

RESULTS_BASE="/mnt/biocluster/praktikum/genprakt/gruppe_g/dual_clustering"
BAM_DIR="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/mapped/minimap2"
VALIDATION_JAR="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/mapValidation/target/validation-jar-with-dependencies.jar"   # <-- Set this to the real path

# Find all clusterHeaders.txt files under the results directory
find "$RESULTS_BASE" -type f -name 'clusterHeaders.txt' | while read -r cluster_file; do
    dir=$(dirname "$cluster_file")
    sample_id=$(basename "$(dirname "$dir")")
    bam_path="$BAM_DIR/${sample_id}.sorted.bam"
    bam_txt="$dir/bam_path.txt"

    # Write bam_path.txt
    if [[ -f "$bam_path" ]]; then
        echo "$bam_path" > "$bam_txt"
    else
        echo "WARNING: BAM file not found for sample $sample_id at $bam_path" >&2
        continue
    fi

    # Set output names
    out_txt="$dir/validation_output.txt"
    outBAM_txt="$dir/bam_clusters.txt"

    # Run validation JAR
    echo "Validating sample $sample_id in $dir"
    java -Xmx50g -jar "$VALIDATION_JAR" \
        -predicted "$cluster_file" \
        -bam "$bam_path" \
        -out "$out_txt" \
        -outBAM "$outBAM_txt" \
        2>&1 | tee "$dir/validation_log.txt"
        
done

echo "Validation complete for all clusters."