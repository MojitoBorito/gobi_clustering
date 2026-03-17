# SUCC - Sequence and UMI Clustering-based correction

Program Excecution:
java -jar clustering.jar 
  -reads \<Read FASTQ> 
  -umi \<UMI FASTQ> 
  -kmer_size \<kmer_size> 
  -threshold \<Error rate> 
  -read_length \<read length> 
  -umi_length \<umi_length>
  -outDir \<Output directory>

Program Output:
- clusters.txt: Consensus UMI, Read and cluster size after primary clustering.
- finer_clusters.txt: Consensus UMI, Read and cluster size after secondary clustering.
- umi_counts.txt: Consensus UMI and their counts
- anchor_counts.txt: Anchor Sequence, Number of Corrected UMI clusters and number of reads clustered in the Anchor Cluster
- pos_mutations.txt: Number of times a substitution was performed at that place.
- base_mutations.txt: Number of times a the specifc base was replaced with the other.
- cluster_headers.txt: reads clustered under same Cluster after primary clustering.
- secondary_clusters.txt: reads clustered under same Cluster after secondary clustering.
