#!/bin/bash

# Base directory
BASE_DIR="com/rental/camp"

# Domains to create
DOMAINS=("user" "order" "rental" "community" "coupon")

# Subfolders for each domain
SUBFOLDERS=("controller" "dto" "model" "repository" "service")

# Loop through each domain and create subfolders with .gitkeep files
for domain in "${DOMAINS[@]}"; do
  # Create domain folder under base directory
  DOMAIN_DIR="$BASE_DIR/$domain"
  mkdir -p "$DOMAIN_DIR"

  # Create subfolders within each domain folder and add .gitkeep file
  for subfolder in "${SUBFOLDERS[@]}"; do
    mkdir -p "$DOMAIN_DIR/$subfolder"
    touch "$DOMAIN_DIR/$subfolder/.gitkeep"
  done
done

echo "폴더 및 .gitkeep 파일 생성 완료!"
