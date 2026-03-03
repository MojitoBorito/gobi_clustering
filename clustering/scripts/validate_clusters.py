import sys
import pandas as pd
print("Interpreter:", sys.executable)

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
        if right > left:
            total += right - left

        # advance the interval that ends first
        if a1 <= b1:
            i += 1
        else:
            j += 1
    return total



#df = pd.read_csv("/home/nikmits/Desktop/uni/WS2526/GoBi/Projects/Clustering/clustering/files/simulation/read.mappinginfo", delimiter='\t')
#df['fw_regvec'] = df["fw_regvec"].map(parse_reg_vec)
#df['rw_regvec'] = df["rw_regvec"].map(parse_reg_vec)
#print(df.dtypes)

print(overlap_len_blocks([(1, 5), (10, 15)], [(6, 9)]))


