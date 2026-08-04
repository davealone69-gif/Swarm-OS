# Swarm OS

**The operating system for your entire AI agent ecosystem.**

This is not just a task list. Every AI agent, project, build, bug, and release flows through this board. It is the single command centre.

---

## Board Philosophy

Treat this like an operating system:
- Agents are processes
- Projects are long-running services
- Builds are jobs
- Bugs are interrupts
- Releases are system updates

Everything visible. Everything trackable. Everything expandable as automation grows.

---

## Recommended Board Structure (GitHub Projects / Linear / Notion)

### Status Columns
| Status              | Meaning                                      |
|---------------------|----------------------------------------------|
| **Inbox / New**     | Just landed                                  |
| **Triaged**         | Understood & prioritised                     |
| **Agent Assigned**  | Handed to a specific AI agent                |
| **In Progress**     | Actively being worked                        |
| **Blocked**         | Waiting on something                         |
| **Review / QA**     | Human or agent review                        |
| **Done / Released** | Shipped                                      |
| **Archived**        | Closed & no longer active                    |

### Custom Fields
- **Type**: `Agent` · `Project` · `Build` · `Bug` · `Release` · `Research` · `Infra`
- **Priority**: `P0` / `P1` / `P2` / `P3`
- **Owner / Agent**: free-text or select (Coder, Researcher, Deployer, Reviewer, etc.)
- **Related Repo**: link or short text
- **Due / Target Date**
- **Iteration / Sprint** (optional)

---

## How to Use This Repo as Command Centre

1. **Create a GitHub Project** linked to this repo (once permissions allow).
2. Use the issue templates below for every new item.
3. Apply labels consistently.
4. Link every agent, project, build, bug, and release as issues or project items.

---

## Getting Started Checklist

- [ ] Re-authorize GitHub connector with Projects permission
- [ ] Create GitHub Project named "Swarm OS" and link this repo
- [ ] Add the custom fields listed above
- [ ] Create the status columns
- [ ] Seed initial Agent definition cards
- [ ] Move existing projects/builds/bugs into the board

---

## Labels (create these in the repo)

### Type
- `type:agent`
- `type:project`
- `type:build`
- `type:bug`
- `type:release`
- `type:research`
- `type:infra`

### Priority
- `priority:P0`
- `priority:P1`
- `priority:P2`
- `priority:P3`

### Status helpers (optional)
- `status:blocked`
- `status:needs-review`

---

## Agent Registry

Create one issue per persistent AI agent using the Agent template. Keep them open and update their status as they work.

---

This foundation expands over time. Start simple. Add automation later.

---

## Android Build — Offline / Vendored AGP Setup

This repo uses **Option A: Local Maven Repository** for fully offline Android Gradle Plugin (AGP) builds. Unlike `flatDir`, a local Maven-layout repository supports plugin marker resolution, POM-based transitive dependency fetching, and `maven-metadata.xml` — everything Gradle needs.

### One-time setup (requires internet)

```bash
# Requires JDK 17+ and Maven (mvn) in PATH
./scripts/vendor-agp.sh
```

This populates `libs/maven-repo/` with AGP 8.4.2 and all transitive dependencies. Commit the populated directory (jars excluded by `.gitignore` — use Git LFS or a binary artifact store for those).

### Offline builds

```bash
./gradlew assembleDebug --offline
./gradlew assembleRelease --offline
```

### Updating AGP

1. Edit `gradle/libs.versions.toml` → change `agp = "X.Y.Z"`
2. Edit `scripts/vendor-agp.sh` → change `AGP_VERSION="X.Y.Z"`
3. Re-run `./scripts/vendor-agp.sh`
4. Commit the updated `libs/maven-repo/`

### Why not `flatDir`?

`flatDir` repositories do not support Maven metadata, POM transitive dependency resolution, or plugin marker artifacts. See `libs/maven-repo/README.txt` for details.
