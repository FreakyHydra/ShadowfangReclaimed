# TODO / Backlog Skill

Use this skill when a bug is found that cannot be immediately fixed, or when a feature request should be addressed later.

## When to Use

Activate this skill when:
- A bug is discovered but cannot be fixed immediately
- A feature request is made that won't be implemented in the current session
- A technical debt item is identified
- Any issue needs to be tracked for later

## How to Use

1. Read the current `TODO.md` file
2. Determine the appropriate section (Bugs, Feature Ideas, Technical Debt)
3. Fill out the template with as much detail as possible
4. Write the updated content to `TODO.md`
5. Commit with message: `docs: Add TODO items for <brief description>`

## Templates

### For Bugs

```markdown
### Bug Title (Priority: High/Medium/Low)
**Status:** ON HOLD

**Description:** Clear description of the bug.

**Root Cause:** What's causing the bug (if known).

**How to Reproduce:** Steps to reproduce (if known).

**Fix:** Outline of how to fix it (if known).

**Files to modify:** List of files that would need changes.
```

### For Feature Ideas

```markdown
### Feature Name (Priority: High/Medium/Low)
**Status:** NEW

**Requested by:** Who asked for it (if applicable).

**Description:** What the feature should do.

**Implementation Notes:** Any ideas on how to implement it.
```

## Priority Levels

| Priority | Meaning |
|----------|---------|
| **High** | Affects gameplay, should be fixed soon |
| **Medium** | Annoying but workaround exists |
| **Low** | Cosmetic or minor inconvenience |

## Status Values

| Status | Meaning |
|--------|---------|
| **ON HOLD** | Bug that cannot be fixed yet |
| **NEW** | Newly identified, not yet started |
| **IN PROGRESS** | Being worked on |
| **DONE** | Resolved (remove from file) |
