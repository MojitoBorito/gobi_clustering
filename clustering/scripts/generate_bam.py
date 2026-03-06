import pandas as pd
import pysam

def parse_regvec(rv: str):
    blocks = []
    for part in rv.split("|"):
        s, e = part.split("-")
        blocks.append((int(s), int(e)))
    blocks.sort()
    return blocks

def extract_mutation_blocks(mutations: str):
    snp_list = [int(x) for x in mutations.split(',')]
    prev_mut = snp_list.pop(0)
    block_start = prev_mut
    block_end = prev_mut
    blocks = []
    for snp in snp_list:
        if prev_mut != snp-1:
            block_end = prev_mut
            blocks.append((block_start, block_end))
            block_start = snp
        prev_mut = snp
    return blocks


def cigar_from_blocks(blocks):
    # end-inclusive coordinates
    cigar = []
    for i, (s, e) in enumerate(blocks):
        mlen = e - s
        cigar.append((0, mlen))  # 0 = M in pysam
        if i + 1 < len(blocks):
            next_s = blocks[i+1][0]
            nlen = next_s - e
            if nlen < 0:
                raise ValueError(f"Overlapping/unsorted blocks: {blocks}")
            if nlen > 0:
                cigar.append((3, nlen))  # 3 = N (ref skip)
    return cigar

def leftmost_pos(blocks):
    return blocks[0][0] - 1  # pysam uses 0-based reference start

def write_bam(mappinginfo_tsv, out_bam, contigs, fw_sequence_phred_dict, rw_sequence_phred_dict):
    """
    contigs: list of (name, length), e.g. [("19", 58617616)] or [("chr19", ...)]
    You must supply lengths, BAM header requires them.
    """
    df = pd.read_csv(mappinginfo_tsv, sep="\t")

    header = {
        "HD": {"VN": "1.6"},
        "SQ": [{"SN": name, "LN": length} for name, length in contigs],
    }

    with pysam.AlignmentFile(out_bam, "wb", header=header) as bam:
        for _, r in df.iterrows():
            qname = str(r["readid"])
            rname = str(r["chr"])

            fw_blocks = parse_regvec(r["fw_regvec"])
            rw_blocks = parse_regvec(r["rw_regvec"])

            a1 = pysam.AlignedSegment()
            a1.query_name = qname
            fw_seq_and_phred = fw_sequence_phred_dict[qname]
            a1.query_sequence = fw_seq_and_phred[0]
            #a1.query_alignment_qualities = pysam.qualitystring_to_array(fw_seq_and_phred[1])
            a1.reference_id = bam.get_tid(rname)
            a1.reference_start = leftmost_pos(fw_blocks)
            a1.cigartuples = cigar_from_blocks(fw_blocks)
            a1.mapping_quality = 60
            a1.flag = 99  # paired + proper + read1 forward, mate reverse (typical)
            a1.next_reference_id = bam.get_tid(rname)
            a1.next_reference_start = leftmost_pos(rw_blocks)
            a1.template_length = (rw_blocks[-1][1] - fw_blocks[0][0] + 1)

            a2 = pysam.AlignedSegment()
            a2.query_name = qname
            rw_seq_and_phred = rw_sequence_phred_dict[qname]
            a2.query_sequence = rw_seq_and_phred[0]
            #a2.query_alignment_qualities = pysam.qualitystring_to_array(rw_seq_and_phred[1])            
            a2.reference_id = bam.get_tid(rname)
            a2.reference_start = leftmost_pos(rw_blocks)
            a2.cigartuples = cigar_from_blocks(rw_blocks)
            a2.mapping_quality = 60
            a2.flag = 147  # paired + proper + read2 reverse (typical)
            a2.next_reference_id = bam.get_tid(rname)
            a2.next_reference_start = leftmost_pos(fw_blocks)
            a2.template_length = -a1.template_length

            bam.write(a1)
            bam.write(a2)

def extract_contigs_from_fidx(path: str):
    df = pd.read_csv(path, delimiter='\t', names=['name', 'length', 'entry_start', 'line_length', 'line_length_with_nn'])
    return [tuple(r) for r in df[['name', 'length']].to_numpy()]

def generate_bam(fw_path, rw_path, mappinginfo_tsv, out_bam, contigs_path):
    fw_sequence_phred_dict = extract_from_fastq(fw_path)
    rw_sequence_phred_dict = extract_from_fastq(rw_path)
    contigs = extract_contigs_from_fidx(contigs_path)
    write_bam(mappinginfo_tsv, out_bam, contigs, fw_sequence_phred_dict, rw_sequence_phred_dict)

def extract_from_fastq(path: str):
    fastq_dict = {}
    with pysam.FastxFile(path) as fh:
        for entry in fh:
            fastq_dict[entry.name] = (entry.sequence, entry.quality)
    return fastq_dict

generate_bam('files/simulation/mock_generation/gen_output/fw.fastq', 'files/simulation/mock_generation/gen_output/rw.fastq',"files/simulation/mock_generation/gen_output/read.mappinginfo", 'files/simulation/mock_generation/OUT.bam', 'files/simulation/input/fixed.fai')