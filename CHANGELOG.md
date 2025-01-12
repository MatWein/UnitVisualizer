# UnitVisualizer Changelog

## [1.8.0]
### Changed
- Updated plugin for IDEA 2024.3

## [1.7.3]
### Changed
- Updated plugin for IDEA 2022.3

## [1.7.2]
### Added
- Allow moving test classes when projects contain multiple test roots, for example java and groovy.

## [1.7.1]
### Added
- Detect test classes with suffix "*Test" and "*Tests"

## [1.7.0]
### Changed
- Upgraded plugin to newest IDEA version 2021.3 and switched from deprecated APIs to newer ones.

## [1.6.2]
### Fixed
- Fixed bug in multi module gradle projects where test classes were not moved in some cases. See https://github.com/MatWein/UnitVisualizer/issues/1 for more information.

## [1.6.1]
### Fixed
- Allow plugin to be used with future versions of IDEA.

## [1.6.0]
### Added
- Migrated plugin code to gradle project structure to match new IntelliJ plugin development SDK.

## [1.5.0]
### Added
- Move test classes when moving whole directories.

## [1.4.1]
### Fixed
- Fixed invalid import.

## [1.4.0]
### Added
- Added layered icons for tested methods.

## [1.3.0]
### Added
- Plugin now supports/works for gradle projects.

## [1.2.3]
### Fixed
- Fixed bug that sometimes test classes will be moved to src/test/resources instead of src/test/java.

## [1.2.2]
### Fixed
- Fixed NullPointerException.

## [1.2.1]
### Fixed
- Fixed development bug in 1.2.

## [1.2.0]
### Fixed
- Fixed move test classes did not work when moving resource files along with java files.

## [1.1.4]
### Added
- Move test class on using quickfix for 'MoveToPackageFix'.

## [1.1.3]
### Fixed
- Fixed some indexing/caching problems.

## [1.1.2]
### Fixed
- Classes and its tests can now be moved between different modules.

## [1.1.1]
### Fixed
- Small fixes in plugin.xml.

## [1.1.0]
### Added
- Added sync test class movement and settings.

## [1.0.0]
### Added
- Initial version.
