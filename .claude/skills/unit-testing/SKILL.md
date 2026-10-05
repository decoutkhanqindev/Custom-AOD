---
name: unit-testing
description: Smart Kotlin unit testing with auto-detection, coverage analysis, and AI edge cases. Use when generating tests for UseCases, mappers / UiModel logic and MVI ViewModels, analyzing test coverage gaps, or discovering edge cases.
---

## Platform Tooling

- Use AskUserQuestion for blocking user choices or confirmations.
- Use WebFetch/WebSearch for current external docs or public web research.
- Use the Agent tool with subagent_type for delegation.
- Use Skill tool when chaining to another installed skill.

# Kotlin Unit Test

Project-specific skill for intelligent Kotlin unit testing.

## When to Use

- Generating tests for business logic: `domain/usecase`, mappers and UiModel logic in `presentation/model` and `data/mapper`, ViewModels (Intent → State / Effect)
- Analyzing test coverage gaps
- Discovering edge cases for existing code
- Before writing new tests to understand what's missing

## Workflow

1. **Analyze class**: `python scripts/analyze_kotlin.py units <file.kt>`
2. **Check coverage**: `python scripts/analyze_kotlin.py coverage <source_dir> <test_dir>`
3. **Discover edge cases**: Load `references/edge-case-discovery.md`
4. **Write tests**: Follow patterns in `references/test-patterns.md`

## Script Commands

```bash
# Analyze single file - extract testable units
python scripts/analyze_kotlin.py units app/src/main/java/com/decoutkhanqindev/custom_aod/domain/usecase/RefreshWeatherUseCase.kt

# Analyze coverage gaps between source and test directories
python scripts/analyze_kotlin.py coverage app/src/main/java/com/decoutkhanqindev/custom_aod/domain app/src/test/java/com/decoutkhanqindev/custom_aod/domain
```

## Output Format

JSON structured output for parsing:
```json
{
  "class_name": "SomeClass",
  "methods": [...],
  "dependencies": [...],
  "coverage": {"tested": [], "missing": []}
}
```

## References

| File | Purpose |
|------|---------|
| `references/test-patterns.md` | JUnit4 + MockK + Turbine patterns |
| `references/edge-case-discovery.md` | AI prompts for finding edge cases |

## Integration

This skill is standalone. If a dedicated test agent is added later, put it in `.claude/agents/` and delegate with the Agent tool.

## Project notes (Custom-AOD)

`CLAUDE.md` at the project root is the source of truth; when this skill and `CLAUDE.md` disagree, follow `CLAUDE.md`.

- Tests live in `app/src/test/java/com/decoutkhanqindev/custom_aod/...`, mirroring the source package (CLAUDE.md § 17).
- Already in the project: JUnit 4, `kotlinx-coroutines-test`. MockK and Turbine are NOT dependencies yet — adding them needs the user's approval (CLAUDE.md hard rules); without them, write fakes by hand.
- Fake at the Repository interface boundary (`domain/repository`); do not mock UseCases or final classes. Managers are concrete Android classes — test ViewModels that depend on them with instrumented tests, or extract the pure logic into the UiModel / mapper and unit-test that.
- UseCases return `Result<T>` built with `suspendRunCatching`; assert `isSuccess` / `exceptionOrNull()`.
- ViewModels extend `BaseViewModel` and launch in `viewModelScope`: set `Dispatchers.setMain(StandardTestDispatcher(testScheduler))`, send intents with `onIntent(...)`, then read `state.value` / collect `effect`.
- Repository implementations switch to `Dispatchers.IO` inside `withContextCatching`; that work runs outside virtual time, so keep Repository tests small or test through a fake.
