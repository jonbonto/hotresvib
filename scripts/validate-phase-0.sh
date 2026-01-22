#!/bin/bash

required_files=(
  "docs/phase-0/architecture.md"
  "docs/phase-0/domain-boundaries.md"
  "docs/phase-0/api-standards.md"
)

for file in "${required_files[@]}"; do
  if [ ! -f "$file" ]; then
    echo "❌ Missing required file: $file"
    exit 1
  fi
done

echo "✅ Phase 0 validation passed"
