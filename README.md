# SUCC - Sequence and UMI Clustering-based correction

Motivation: PCR amplification in RNA-seq can lead to disproportionate duplication of certain reads, which may
result in inflated expression estimates and increases the computational workload for read mapping. Unique molecular
identifiers (UMIs) are therefore used to label individual fragments prior to amplification. By using UMIs, it
becomes possible to identify reads originating from the same molecule, enabling deduplication and correction of
sequencing errors. However, widely used deduplication tools require reads to be mapped to a reference genome
before deduplication can be performed. While this approach removes PCR-induced bias in expression estimates, the
computational cost of mapping duplicated reads remains. Clustering-based approaches provide an alternative strategy
to address this limitation by grouping potential duplicate reads prior to alignment using both UMI information and
sequence similarity between reads.
Results: In this report, we present a reliable method to deduplicate reads and correct sequencing errors by clustering
both UMI sequences and read anchors. The tool implements a two-cycle clustering strategy, where each cycle is
optional and configurable depending on the dataset characteristics. Using simulated data, the method successfully
clusters PCR duplicates and constructs consensus sequences for each cluster, enabling the correction of sequencing
errors.

## Usage

The main program is **clustering.jar**.

### Running the Clustering Tool

```sh
java -jar clustering.jar \
  -reads <input_reads.fastq> \
  -umi <input_umi.fastq> \
  -kmer_size <int> \
  -read_length <int> \
  -umi_length <int> \
  -threshold <float> \
  -outDir <output_directory>
```

#### Parameters

| Parameter        | Description                                               | Required |
|------------------|----------------------------------------------------------|----------|
| `-reads`         | Path to the FASTQ file containing read sequences          | Yes      |
| `-umi`           | Path to the FASTQ file containing UMI sequences           | Yes      |
| `-kmer_size`     | Length of positional k-mers for clustering (integer)      | Yes      |
| `-read_length`   | Length of reads in the input FASTQ file (integer)         | Yes      |
| `-umi_length`    | Length of UMIs in the input FASTQ file (integer)          | Yes      |
| `-threshold`     | Maximum allowed error rate in the read sequence (float)   | Yes      |
| `-outDir`        | Directory for output files                                | Yes      |
