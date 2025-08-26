#!/bin/bash

# Exit on error
set -e

# Get the kraftshade version from the toml file
echo "Getting kraftshade version from toml file..."
kraftshade_version=$(grep -o 'kraftshade = { group = "com.cardinalblue", name = "kraftshade", version = "[0-9]*\.[0-9]*\.[0-9]*" }' gradle/libs.versions.toml | grep -o '[0-9]*\.[0-9]*\.[0-9]*')

if [ -z "$kraftshade_version" ]; then
    echo "Error: Could not find kraftshade version in gradle/libs.versions.toml"
    exit 1
fi

echo "Found kraftshade version: $kraftshade_version"

# Publish to Maven Central
echo "Publishing kraftshade-media3 to Maven Central..."
./gradlew :kraft-shade-media3:publishAndReleaseToMavenCentral -Ppublish_version="$kraftshade_version"

# Update version in libs.versions.toml
echo "Updating version in gradle/libs.versions.toml..."
sed -i '' "s/kraftshade-media3 = { group = \"com.cardinalblue\", name = \"kraftshade-media3\", version = \"[0-9]*\.[0-9]*\.[0-9]*\" }/kraftshade-media3 = { group = \"com.cardinalblue\", name = \"kraftshade-media3\", version = \"$kraftshade_version\" }/" gradle/libs.versions.toml

# Commit changes
echo "Committing changes..."
git add kraft-shade-media3/build.gradle.kts gradle/libs.versions.toml
git commit -m "chore: update kraftshade-media3 version to $kraftshade_version"

# Push changes
echo "Pushing changes and tag..."
git push origin main

echo "kraftshade-media3 $kraftshade_version has been published successfully!"