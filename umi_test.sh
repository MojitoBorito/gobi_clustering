
FASTQ_DIR="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq"

BLOCK_BASE="/mnt/biocluster/praktikum/genprakt/patil/Blockteil"
RESULTS_BASE_NAME="umi_results"   # name of the top-level directory for all sampless
RESULTS_BASE="$BLOCK_BASE/$RESULTS_BASE_NAME"

JAR="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/clustering/target/clustering-jar-with-dependencies.jar"

mkdir -p "$RESULTS_BASE"

for UMI in "$FASTQ_DIR"/H*-T2_R2_001.fastq.gz; do
    fname=$(basename "$UMI")    

    sample_id=${fname%%_R2_001.fastq.gz}

    SAMPLE_DIR="$RESULTS_BASE/$sample_id"

    PROB_DIR="$SAMPLE_DIR/probs"
    COUNT_DIR="$SAMPLE_DIR/counts"

    mkdir -p "$PROB_DIR" "$COUNT_DIR"

    echo "Processing sample $sample_id"
    echo "  Input:  $UMI"
    echo "  Probs:  $PROB_DIR"
    echo "  Counts: $COUNT_DIR"

    for i in {0..3}; do
        prob_out="$PROB_DIR/prob_$i.txt"
        count_out="$COUNT_DIR/count_$i.txt"

        java -jar "$JAR" -umi "$UMI" -h "$i" -probs "$prob_out" -counts "$count_out"
    done
done