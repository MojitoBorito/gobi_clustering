#!/usr/bin/env bash

# Usage:
#   ./test_reader.sh genome.fa genome.fa.fai MyReader.jar 50
#                     $1        $2          $3            $4
#                                                   ↑ number of tests (optional, default 20)

FASTA="$1"
FAI="$2"
JAR="$3"
NUM_TESTS="${4:-20}"   # default: 20 random tests

if [[ -z "$FASTA" || -z "$FAI" || -z "$JAR" ]]; then
  echo "Usage: $0 <fasta> <fai> <jar> [num_tests]"
  exit 1
fi

# check tools
if ! command -v samtools >/dev/null 2>&1; then
  echo "samtools not found in PATH. Install samtools first."
  exit 1
fi

# read sequence names from the .fai (first column)
mapfile -t SEQS < <(cut -f1 "$FAI")

# function to fail nicely
fail() {
  echo "❌ $1"
  exit 1
}

for ((i=1; i<=NUM_TESTS; i++)); do
  # pick random seq
  seq="${SEQS[$RANDOM % ${#SEQS[@]}]}"

  # get its length from fai (2nd column)
  seq_len=$(awk -v s="$seq" '$1==s {print $2}' "$FAI")
  if [[ -z "$seq_len" ]]; then
    fail "Could not find length for sequence $seq in $FAI"
  fi

  # choose a random length for the test fragment (e.g. 50–300)
  frag_len=$(( 50 + RANDOM % 251 ))  # 50..300

  # choose random start so that start+frag_len-1 <= seq_len
  max_start=$(( seq_len - frag_len ))
  if (( max_start < 1 )); then
    # sequence is shorter than fragment length; skip
    ((i--))
    continue
  fi
  start=$(( 1 + RANDOM % max_start ))
  end=$(( start + frag_len - 1 ))

  echo "[$i/$NUM_TESTS] Testing $seq:$start-$end"

  # get reference using samtools
  samtools faidx "$FASTA" "$seq:$start-$end" > /tmp/ref.fa || fail "samtools failed"

  # get output from your reader
  # assuming your main is: java -jar MyReader.jar <fai> <fasta> <seq> <start> <end>
  java -jar "$JAR" "$FAI" "$FASTA" "$seq" "$start" "$end" > /tmp/test.fa || fail "java reader failed"

  # normalize both: drop header lines, remove whitespace
  ref_seq=$(grep -v '^>' /tmp/ref.fa | tr -d ' \n\r\t')
  test_seq=$(grep -v '^>' /tmp/test.fa | tr -d ' \n\r\t')

  if [[ "$ref_seq" != "$test_seq" ]]; then
    echo "❌ MISMATCH for $seq:$start-$end"
    echo "Ref:  $ref_seq"
    echo "Test: $test_seq"
    exit 1
  else
    echo "✅ OK"
  fi
done

echo "🎉 All $NUM_TESTS tests passed."
