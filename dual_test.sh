UMI="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R2_001.fastq.gz"
FW="/mnt/raidbio2/extdata/praktikum/genprakt/genprakt-ws25/Block/pig-data-rnaseq/H5-12939-T2_R1_001.fastq.gz"
OUT="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/dual_out2/clusters.txt"
JAR="/mnt/biocluster/praktikum/genprakt/patil/Blockteil/clustering/target/clustering-jar-with-dependencies.jar"

java -jar $JAR -umi $UMI -out $OUT -reads $FW
