#!/bin/bash
# Build the judge sandbox image before running docker compose.
# Run this once (or after modifying sandbox/Dockerfile):
#   bash build-sandbox.sh
set -e
echo "Building judge-sandbox:latest..."
docker build -t judge-sandbox:latest ./services/judge-service/sandbox
echo "Done. You can now run: docker compose up --build"
