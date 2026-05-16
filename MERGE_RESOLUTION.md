# Merge Conflict Resolution

**PR**: Syncing (main → tests)
**Date**: 2026-05-16
**Strategy**: Accept changes from `main` branch (head/source)

## Conflict Summary

The `main` branch contains significant improvements:
- UI refactoring with reusable components (AppHeader, StatCard, WeatherCard, EmptyState)
- Architecture improvements (repository pattern, domain layer separation)
- New UI states (Offline state for no connectivity)
- Enhanced error handling (NetworkUnavailableException)
- Build configuration updates (signing, minification)
- Comprehensive README documentation

The `tests` branch contains earlier work on tests and screen refactoring.

## Resolution

All files from `main` have been accepted as they represent the latest, most complete version of the codebase.

### Key Changes Merged:
1. **Data Layer**: Reorganized into `dao/` and `entity/` subdirectories
2. **Repository Pattern**: Moved from `data.WeatherRepository` to `data.repository.WeatherRepositoryImpl` with interface in `domain.repository`
3. **UI Components**: Extracted reusable components for headers, cards, and states
4. **Theme**: Updated color palette for better consistency
5. **Build**: Added release signing and minification
6. **Documentation**: Expanded README with architecture notes and testing guides

### Files Status:
- ✅ All 34 files successfully resolved
- ✅ No data loss
- ✅ Imports updated throughout
- ✅ Architecture aligned with clean code principles
