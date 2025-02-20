#!/bin/bash

HEADER_FILE="docs/readme_header.md"
PYTHON_SCRIPT="scripts/readme_table_gen.py"
OUTPUT_FILE="README.md"

if [ ! -f "$HEADER_FILE" ]; then
  echo "Errore: il file $HEADER_FILE non esiste."
  exit 1
fi
#
if [ ! -f "$PYTHON_SCRIPT" ]; then
  echo "Errore: lo script Python $PYTHON_SCRIPT non esiste."
  exit 1
fi

{
  cat "$HEADER_FILE"
  echo ""
  python3 "$PYTHON_SCRIPT" .
} > "$OUTPUT_FILE"

echo "README.md aggiornato con successo."
