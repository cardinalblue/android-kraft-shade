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
echo "Publishing kraftshade-compose to Maven Central..."
./gradlew :kraft-shade-compose:publishAndReleaseToMavenCentral -Ppublish_version="$kraftshade_version"

# Update version in libs.versions.toml
echo "Updating version in gradle/libs.versions.toml..."
sed -i '' "s/kraftshade-compose = { group = \"com.cardinalblue\", name = \"kraftshade-compose\", version = \"[0-9]*\.[0-9]*\.[0-9]*\" }/kraftshade-compose = { group = \"com.cardinalblue\", name = \"kraftshade-compose\", version = \"$kraftshade_version\" }/" gradle/libs.versions.toml

# Commit changes
echo "Committing changes..."
git add kraft-shade-compose/build.gradle.kts gradle/libs.versions.toml
git commit -m "chore: update kraftshade-compose version to $kraftshade_version"

# Create tag
echo "Creating tag $kraftshade_version..."
git tag "$kraftshade_version"

# Push changes and tag
echo "Pushing changes and tag..."
git push origin main
git push origin "$kraftshade_version"

echo "kraftshade-compose $kraftshade_version has been published successfully!"
echo "Tag $kraftshade_version has been created and pushed."