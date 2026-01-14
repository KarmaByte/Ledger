# Contributing to Ledger

Thank you for your interest in contributing to Ledger! This document provides guidelines for contributing to the project.

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow

## How to Contribute

### Reporting Bugs

1. Check existing issues to avoid duplicates
2. Use the bug report template
3. Include:
   - Ledger version
   - Hytale server version
   - Steps to reproduce
   - Expected vs actual behavior
   - Logs/errors if applicable

### Suggesting Features

1. Check existing feature requests
2. Use the feature request template
3. Explain the use case and benefits
4. Consider backwards compatibility

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Write/update tests if applicable
5. Ensure code compiles: `./gradlew build`
6. Commit with clear messages
7. Push and create a PR

## Development Setup

### Prerequisites

- Java 25
- Gradle 8.x (wrapper included)

### Building

```bash
./gradlew build
```

### Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions
- Add Javadoc to public methods
- Keep methods focused and small

### Commit Messages

Format: `type: description`

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance

Examples:
- `feat: add multi-currency support`
- `fix: handle negative amounts correctly`
- `docs: update API examples`

## Architecture

```
Ledger/
├── api/          # Public API interfaces (what plugins depend on)
└── core/         # Implementation (not for external use)
```

### Guidelines

1. **API Stability**: Changes to `api/` module must be backwards compatible
2. **Async First**: Use `CompletableFuture` for I/O operations
3. **No Nulls**: Use `Optional` and `@NotNull/@Nullable` annotations
4. **Events**: Fire events for state changes
5. **Documentation**: All public APIs must have Javadoc

## Review Process

1. All PRs require at least one review
2. CI must pass
3. No merge conflicts
4. Documentation updated if needed

## Questions?

- Open an issue with the "question" label
- Join our Discord server

Thank you for contributing!
