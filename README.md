# Clustering
  - Without UMIs

## Simulation:
The parameters are same as Readsimulator, but with 3 extra parameters. 
1. -fragFreq: tells how often a fragment should appear
2. -shift: tells how many fragments we want to produce for the fragment from transcript
3. -diff: tells the offset between shifted fragments

Remember: ReadCounts needs to be reduced, because the the number of created Sequences per transcript is readcounts * fragFreq * shifted

## Targets:
- Decide meter of measurement
  - Alignment
  - Hamming distance
  - K-mer

## Decide clustering idea
- K-means
- Hierarchical clustering

## Minimizers

## Idea 1: 
- Extract k-mers (Decide on fixed random positions or all)
- look up k-mers in the bucket of each cluster, if buckets match:
  - no: create new cluster with sequence as seed
  - yes: compute hamming distance with each seed candidate
    - If any sequence with hamming distance =< threshold:
      - no: create new cluster with sequence as seed
      - yes: add sequence to cluster( and then update the cluster seed, i.e. update consensus string to make step with hamming distance faster.)


       New Read arrives
                    │
                    ▼
              ┌─ Extract k-mers, look ─┐
              │  up in bucket tables    │
              └──────────┬─────────────┘
                         │
                         ▼
                  Any buckets match?
                   /           \
                 NO             YES
                 │               │
                 ▼               ▼
          Create new      Hamming vs candidate
          cluster         CONSENSUS seeds
          (init counts)        │
                               ▼
                        distance ≤ threshold?
                         /            \
                       NO              YES
                       │                │
                       ▼                ▼
                 Create new       Assign to cluster
                 cluster          │
                                  ▼
                            hamming == 0?
                             /        \
                           YES         NO
                           │            │
                           ▼            ▼
                      Just update   Update counts AND
                      counts        rebuild consensus
                      (consensus    (seed may change)
                       unchanged)        │
                                         ▼
                                  ┌──────────────────┐
                                  │ Update k-mer     │
                                  │ buckets if the   │
                                  │ consensus changed │
                                  │ at a sampled     │
                                  │ k-mer position   │
                                  └──────────────────┘
