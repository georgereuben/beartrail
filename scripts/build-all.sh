#!/bin/bash

# BearTrail Build All Services Script

echo "🔨 Building all BearTrail services..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build shared libraries first
echo "📚 Building shared libraries..."
./gradlew :shared:common:build :shared:test-utils:build

# Build all services
echo "🏗️ Building all services..."
./gradlew build -x test

# Run tests
echo "🧪 Running tests..."
./gradlew test

echo "✅ All services built successfully!"
