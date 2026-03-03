
import pandas as pd
import networkx as nx
import os

def parse_reg_vec(rv: str):
    out = []
    for block in rv.split('|'):
        try:
            s, e = block.split('-')
            out.append((int(s), int(e)))
        except ValueError:
            print(block)
            raise ValueError
    return out


def overlap_len_blocks(a_blocks, b_blocks) -> int:
    # total bp overlap across all block pairs
    i = j = 0
    total = 0
    a = sorted(a_blocks)
    b = sorted(b_blocks)

    while i < len(a) and j < len(b):
        a0, a1 = a[i]
        b0, b1 = b[j]

        left = max(a0, b0)
        right = min(a1, b1)
        if right >= left:
            total += right - left + 1

        # advance the interval that ends first
        if a1 <= b1:
            i += 1
        else:
            j += 1
    return total

def total_len(blocks) -> int:
    return sum(e - s + 1 for s, e in blocks)

# Jaccard
def overlap_jaccard(a_blocks, b_blocks) -> float:
    ov = overlap_len_blocks(a_blocks, b_blocks)
    len_a = total_len(a_blocks)
    len_b = total_len(b_blocks)
    denom = len_a + len_b - ov
    return ov / denom if denom else 0.0


def get_all_clusters(input: str, output_dir: str, threshold: float):
    if not 0 < threshold <= 1.0:
        raise ValueError("Invalid threshold")
    df = pd.read_csv(input, delimiter='\t')

    fw_df = pd.DataFrame(df[['readid', 'chr_id', 'gene_id', 'transcript_id', 'fw_regvec', 't_fw_regvec']])
    rw_df = pd.DataFrame(df[['readid', 'chr_id', 'gene_id', 'transcript_id', 'rw_regvec', 't_rw_regvec']])

    fw_df['blocks'] = fw_df['fw_regvec'].map(parse_reg_vec)
    rw_df['blocks'] = rw_df['rw_regvec'].map(parse_reg_vec)

    fw_df["span_start"] = fw_df["blocks"].map(lambda bl: bl[0][0])
    fw_df["span_end"]   = fw_df["blocks"].map(lambda bl: bl[-1][1])
    
    rw_df["span_start"] = rw_df["blocks"].map(lambda bl: bl[0][0])
    rw_df["span_end"]   = rw_df["blocks"].map(lambda bl: bl[-1][1])

    fw_clusters = get_clusters(fw_df, threshold)
    rw_clusters = get_clusters(rw_df, threshold)
    
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    write_clusters(fw_clusters, os.path.join(output_dir, 'fw_clusters'))
    write_clusters(rw_clusters, os.path.join(output_dir, 'rw_clusters'))


def get_clusters(df: pd.DataFrame, threshold: float):
    G = nx.Graph()

    # Add all reads as nodes
    G.add_nodes_from(df.index)

    for (chr_, gene_), group in df.groupby(["chr_id", "gene_id"]):
        
        group = group.sort_values("span_start")
        spans = group[["span_start", "span_end"]].values
        blocks = group["blocks"].values
        idxs = group.index.values
        
        for i in range(len(group)):
            for j in range(i+1, len(group)):
                
                # early break if spans don't overlap
                if spans[j][0] > spans[i][1]:
                    break
                
                sim = overlap_jaccard(blocks[i], blocks[j])
                
                if sim >= threshold:
                    G.add_edge(idxs[i], idxs[j])
    return list(nx.connected_components(G))
    

def write_clusters(clusters, output_path):
    rows = []

    for cluster in clusters:
        # sort for reproducibility
        readids = sorted(cluster)
        rows.append({
            "readids": ",".join(map(str, readids))
        })

    out_df = pd.DataFrame(rows)
    out_df.to_csv(output_path, sep="\t", index=False)



get_all_clusters('files/simulation/read.mappinginfo', 'files/ideal', 0.5)



