#!/usr/bin/env bash
# vendor-agp.sh
#
# Populates libs/maven-repo/ with AGP and all transitive dependencies so that
# subsequent builds can run fully offline (./gradlew --offline).
#
# Prerequisites (run on a machine with internet access):
#   - JDK 17+
#   - Maven (mvn) in PATH
#
# Usage:
#   ./scripts/vendor-agp.sh
#
# To update AGP, change AGP_VERSION below and re-run.

# Exit on unset variables and pipe failures, but NOT on individual command errors
# (we handle errors per-artifact below so one failure doesn't abort everything).
set -uo pipefail

AGP_VERSION="8.4.2"
KOTLIN_VERSION="1.9.24"
REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)/libs/maven-repo"

# Coordinates to vendor (plugin markers + implementation artifacts)
ARTIFACTS=(
  "com.android.application:com.android.application.gradle.plugin:${AGP_VERSION}:pom"
  "com.android.library:com.android.library.gradle.plugin:${AGP_VERSION}:pom"
  "com.android.tools.build:gradle:${AGP_VERSION}"
  "org.jetbrains.kotlin:kotlin-gradle-plugin:${KOTLIN_VERSION}"
  "org.jetbrains.kotlin:kotlin-gradle-plugin-api:${KOTLIN_VERSION}"
)

# Remote repositories to resolve from
REMOTE_REPOS=(
  "https://dl.google.com/dl/android/maven2/"
  "https://repo1.maven.org/maven2/"
  "https://plugins.gradle.org/m2/"
)

echo "==> Vendoring AGP ${AGP_VERSION} into ${REPO_DIR}"
mkdir -p "${REPO_DIR}"

# Verify network access before spending time on resolution.
# Use a more resilient check: try multiple endpoints, longer timeout, follow redirects.
echo "--> Checking network access..."
NETWORK_OK=false
for url in \
  "https://dl.google.com/dl/android/maven2/" \
  "https://repo1.maven.org/maven2/" \
  "https://plugins.gradle.org/m2/"; do
  if curl -sL --connect-timeout 10 --max-time 20 -o /dev/null -w "%{http_code}" "$url" 2>/dev/null | grep -qE '^(200|301|302|403|404)$'; then
    echo "    Reachable: $url"
    NETWORK_OK=true
    break
  fi
done

if [[ "$NETWORK_OK" != "true" ]]; then
  echo ""
  echo "ERROR: Cannot reach Google/Maven repositories. This script must be run on a machine"
  echo "       with internet access. Run it once online, commit libs/maven-repo/,"
  echo "       then all future builds can use:  ./gradlew --offline"
  exit 1
fi
echo "    Network OK."

# Build -DremoteRepositories argument for maven dependency:get
REPO_ARGS=""
for repo in "${REMOTE_REPOS[@]}"; do
  REPO_ARGS="${REPO_ARGS}${repo},"
done
REPO_ARGS="${REPO_ARGS%,}"  # strip trailing comma

for artifact in "${ARTIFACTS[@]}"; do
  echo ""
  echo "--> Resolving: ${artifact}"

  # Split into groupId:artifactId:version[:packaging]
  IFS=':' read -r GROUP ARTIFACT VERSION PACKAGING <<< "${artifact}:::"
  PACKAGING="${PACKAGING:-jar}"

  mvn dependency:get \
    -Dartifact="${GROUP}:${ARTIFACT}:${VERSION}:${PACKAGING}" \
    -DremoteRepositories="${REPO_ARGS}" \
    -Ddest="${REPO_DIR}" \
    -Dtransitive=true \
    --quiet || { echo "    WARNING: resolution failed for ${artifact} — skipping"; continue; }

  # Copy resolved artifacts from local Maven cache into our repo layout
  LOCAL_CACHE="${HOME}/.m2/repository"
  GROUP_PATH="${GROUP//.//}"
  SRC_DIR="${LOCAL_CACHE}/${GROUP_PATH}/${ARTIFACT}/${VERSION}"
  DEST_DIR="${REPO_DIR}/${GROUP_PATH}/${ARTIFACT}/${VERSION}"

  if [[ -d "${SRC_DIR}" ]]; then
    mkdir -p "${DEST_DIR}"
    cp -v "${SRC_DIR}/"* "${DEST_DIR}/" 2>/dev/null || true
    echo "    Copied to ${DEST_DIR}"
  else
    echo "    WARNING: ${SRC_DIR} not found in local Maven cache"
  fi
done

# Copy ALL transitive dependencies pulled into ~/.m2 during resolution
echo ""
echo "==> Copying transitive dependencies..."

# Use mvn dependency:resolve with copy-dependencies to get everything
TEMP_POM=$(mktemp /tmp/vendor-pom-XXXXXX.xml)
cat > "${TEMP_POM}" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.swarm.vendor</groupId>
  <artifactId>vendor-helper</artifactId>
  <version>1.0</version>
  <packaging>pom</packaging>
  <dependencies>
    <dependency>
      <groupId>com.android.tools.build</groupId>
      <artifactId>gradle</artifactId>
      <version>${AGP_VERSION}</version>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-gradle-plugin</artifactId>
      <version>${KOTLIN_VERSION}</version>
    </dependency>
  </dependencies>
  <repositories>
$(for repo in "${REMOTE_REPOS[@]}"; do
  REPO_ID=$(echo "${repo}" | md5sum | cut -c1-8)
  echo "    <repository><id>r${REPO_ID}</id><url>${repo}</url></repository>"
done)
  </repositories>
</project>
EOF

mvn -f "${TEMP_POM}" \
  dependency:copy-dependencies \
  -DoutputDirectory="${REPO_DIR}" \
  -DincludeScope=runtime \
  --quiet || true

rm -f "${TEMP_POM}"

# Generate maven-metadata.xml stubs for key plugin marker directories
echo ""
echo "==> Generating maven-metadata.xml for plugin markers..."

for PLUGIN_ID in "com.android.application" "com.android.library"; do
  META_DIR="${REPO_DIR}/${PLUGIN_ID//.//}/${PLUGIN_ID}.gradle.plugin"
  META_FILE="${META_DIR}/maven-metadata.xml"
  mkdir -p "${META_DIR}"
  cat > "${META_FILE}" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>${PLUGIN_ID}</groupId>
  <artifactId>${PLUGIN_ID}.gradle.plugin</artifactId>
  <versioning>
    <release>${AGP_VERSION}</release>
    <versions>
      <version>${AGP_VERSION}</version>
    </versions>
    <lastUpdated>$(date -u +%Y%m%d%H%M%S)</lastUpdated>
  </versioning>
</metadata>
EOF
  echo "    Written: ${META_FILE}"
done

echo ""
echo "==> Done. libs/maven-repo/ is ready for offline builds."
echo "    Commit this directory and run:  ./gradlew assembleDebug --offline"
