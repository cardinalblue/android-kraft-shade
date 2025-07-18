#!/bin/bash

# Exit on error
set -e

# Fetch the latest tags
echo "Fetching git tags..."
git fetch --tags

# Get the latest version tag using a simpler method
echo "Finding latest version tag..."
latest_version=$(git tag | grep "^\([0-9]\+\.\)\{2\}[0-9]\+$" | sort -V | tail -1)

if [ -z "$latest_version" ]; then
    echo "No version tags found. Using 1.0.0 as the base version."
    latest_version="1.0.0"
fi

echo "Latest version: $latest_version"

# Calculate next version (increment patch)
major=$(echo $latest_version | cut -d. -f1)
minor=$(echo $latest_version | cut -d. -f2)
patch=$(echo $latest_version | cut -d. -f3)
next_patch=$((patch + 1))
next_version="$major.$minor.$next_patch"

echo "Suggested next version: $next_version"

# Ask user for version
read -p "Enter version to publish (press Enter for $next_version): " user_version
if [ -z "$user_version" ]; then
    user_version="$next_version"
    echo "Using default version: $user_version"
else
    echo "Using user-provided version: $user_version"
fi

# Validate version format
if ! [[ $user_version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: Version must be in the format X.Y.Z (e.g., 1.0.34)"
    exit 1
fi

# Publish to Maven Central
echo "Publishing kraftshade to Maven Central..."
./gradlew :kraft-shade:publishAndReleaseToMavenCentral -Ppublish_version="$user_version"

# Update version in libs.versions.toml
echo "Updating version in gradle/libs.versions.toml..."
sed -i '' "s/kraftshade = { group = \"com.cardinalblue\", name = \"kraftshade\", version = \"[0-9]*\.[0-9]*\.[0-9]*\" }/kraftshade = { group = \"com.cardinalblue\", name = \"kraftshade\", version = \"$user_version\" }/" gradle/libs.versions.toml

# Commit changes
echo "Committing changes..."
git add gradle/libs.versions.toml
git commit -m "chore: Update kraftshade version to $user_version"
git push

echo "kraftshade $user_version has been published successfully!"
echo "Note: No tag has been created yet. A tag will be created after publishing kraftshade-compose."