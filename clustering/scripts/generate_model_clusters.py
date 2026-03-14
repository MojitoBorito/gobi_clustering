import pandas as pd
import networkx as nx
import os



def get_all_clusters(input: str, output_dir: str):
    df = pd.read_csv(input, delimiter='\t')

    fw_df = pd.DataFrame(df[['readid', 'chr_id', 'gene_id', 'transcript_id', 'fw_regvec', 't_fw_regvec']])
    rw_df = pd.DataFrame(df[['readid', 'chr_id', 'gene_id', 'transcript_id', 'rw_regvec', 't_rw_regvec']])
    fw_df.rename({'fw_regvec': 'regvec', 't_fw_regvec' : 't_regvec'}, axis='columns', inplace=True)
    rw_df.rename({'rw_regvec': 'regvec', 't_rw_regvec' : 't_regvec'}, axis='columns', inplace=True)

    fw_clusters = get_clusters(fw_df)
    rw_clusters = get_clusters(rw_df)
    
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    write_clusters(fw_clusters, os.path.join(output_dir, 'fw_clusters'))
    write_clusters(rw_clusters, os.path.join(output_dir, 'rw_clusters'))


def get_clusters(df: pd.DataFrame):
    clusters = []
    for (chr_id_, gene_, regvec_), group in df.groupby(["chr_id", "gene_id", "regvec"]):
        clusters.append(group.index.values)
    return clusters
        
    
    

def write_clusters(clusters, output_path):
    rows = []

    for i, cluster in enumerate(clusters):
        # sort for reproducibility
        readids = sorted(cluster)
        for readid in readids:
            rows.append((i, readid))

    out_df = pd.DataFrame(rows, columns=['cluster_id', 'read_id'])
    out_df.to_csv(output_path, sep="\t", index=False)



get_all_clusters('files/simulation/monster_generation/read.mappinginfo', 'files/simulation/monster_generation/true_clusters')
