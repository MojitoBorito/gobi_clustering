UMI="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R2_001.fastq.gz"
FW="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R1_001.fastq.gz"
OUT="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/dual_out2"
JAR="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/clustering/target/clustering-jar-with-dependencies.jar"

java -Xmx100g -jar $JAR -umi $UMI -reads $FW -outDir "$OUT" -umi_length "12"
