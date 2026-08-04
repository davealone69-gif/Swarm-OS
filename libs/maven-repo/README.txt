README — libs/maven-repo
========================

This directory is a local Maven-layout repository used for fully offline/vendored
Android Gradle Plugin (AGP) builds.

Why not flatDir?
----------------
Gradle's flatDir repositories do not support:
  - Maven metadata (maven-metadata.xml)
  - POM files with transitive dependency declarations
  - Plugin marker artifact resolution

This local Maven layout solves all three problems.

Directory structure (pre-seeded stubs)
---------------------------------------
libs/maven-repo/
  com/android/application/
    com.android.application.gradle.plugin/
      8.4.2/
        *.pom          ← Plugin marker POM (redirects to impl artifact)
  com/android/tools/build/
    gradle/
      8.4.2/
        gradle-8.4.2.pom   ← Implementation stub POM
        gradle-8.4.2.jar   ← NOT committed; populated by vendor-agp.sh

Populating the repository
--------------------------
Run the vendor script ONCE on a machine that has internet access:

  ./scripts/vendor-agp.sh

This will:
  1. Resolve AGP 8.4.2 and all transitive dependencies from remote repos.
  2. Copy jars, poms, and maven-metadata.xml files into libs/maven-repo/.
  3. Replace the stub gradle-8.4.2.pom with the real one (full dep list).

After running the script, commit the populated libs/maven-repo/ directory.
All subsequent builds can then run fully offline:

  ./gradlew assembleDebug --offline

Updating AGP version
---------------------
1. Edit gradle/libs.versions.toml  →  agp = "X.Y.Z"
2. Edit scripts/vendor-agp.sh      →  AGP_VERSION="X.Y.Z"
3. Re-run ./scripts/vendor-agp.sh
4. Commit the updated libs/maven-repo/
