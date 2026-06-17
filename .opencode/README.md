# Shadowfang Folia Project — Agent Workspace

This folder contains everything needed for agents to work effectively on the Shadowfang Folia project.

## Quick Start

When working on this project:

1. **Read AGENTS.md** in the project root for project-wide guidelines
2. **Read TODO.md** for known issues and planned features
3. **Use skills** from the `skills/` folder when applicable
4. **Follow versioning rules** from AGENTS.md
5. **Always test** before and after deployment

## Project Structure

```
Shadowfang-Folia-Project/
├── .opencode/
│   ├── skills/           # Agent skills
│   │   ├── todo-skill.md       # Tracking deferred issues
│   │   ├── folia-dev-skill.md # Folia development patterns
│   │   └── testing-skill.md    # Testing & debugging
│   └── README.md             # This file
├── AGENTS.md                 # Project guidelines for agents
├── TODO.md                   # Known issues and backlog
├── README.md                 # Public project documentation
├── Shadowfang-Core-Folia/    # Main plugin
│   └── build.gradle
└── foliaserver/             # Server files (not in git)
    └── plugins/
```

## Available Skills

| Skill | When to Use |
|-------|-------------|
| **todo-skill.md** | When an issue needs to be tracked for later |
| **folia-dev-skill.md** | When writing Folia-specific code |
| **testing-skill.md** | When testing or debugging changes |

## Key Rules

1. **Stop server before replacing JARs** — Server locks plugin files while running
2. **Use Folia schedulers** — Never Bukkit schedulers for Folia plugins
3. **Check git status** — Don't commit unrelated changes
4. **Update TODO** — When deferring an issue, document it properly
5. **Version bumps** — Follow MAJOR.MINOR.PATCH scheme

## Deployment

```powershell
# 1. Build
cd Shadowfang-Core-Folia
./gradlew build

# 2. Stop server
Stop-Process -Name java -Force  # Or use console: /stop

# 3. Copy JAR
Copy-Item build/libs/Shadowfang-Core-Folia-1.3.1.jar `
    -Destination ../foliaserver/plugins/

# 4. Start server
# Run start.bat from foliaserver directory
```

## Git Workflow

1. Check git status before committing
2. Stage only relevant files
3. Write descriptive commit messages
4. Push to origin/main

```bash
git status
git add <files>
git commit -m "type: description

- Added feature X
- Fixed bug Y"
git push origin main
```

## Common Tasks

### Adding a new command
1. Create command class in appropriate package
2. Register in `ShadowfangCorePlugin.java`
3. Add to SrDispatcher
4. Update permissions in plugin.yml
5. Test and deploy

### Fixing a bug
1. Identify root cause
2. Make fix
3. Test locally
4. Update TODO if deferring
5. Bump version (PATCH bump)
6. Deploy and test
7. Commit and push

### Adding a new feature
1. Plan implementation
2. Create feature files
3. Wire into plugin
4. Update README with documentation
5. Bump version (MINOR bump)
6. Deploy and test
7. Commit and push

## Contact

For questions about this project, contact the maintainer.
