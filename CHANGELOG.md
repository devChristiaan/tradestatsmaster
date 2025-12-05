# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased] - 2025-11-28

New Features Planned:

- Business rules setting

### Changed

## [1.7.1] - 2025-12-04

Bug Fix
- Fixed issue with target profit in Stop loss tab not calculating correctly
- Added a logger for proper tracking
- Fixed goals not saving
- Added ctrl + s to save for Daily prep, journal and goals

## [1.7.0] - 2025-11-28

Major Feature Added:
Added a tab so the user can set goal. Goals are divided into 3 categories

- Short term
- Mid term
- Long term

## [1.6.1] - 2025-11-26

Under the hood update. Refactored the Global context pattern to be more stable an extensible.

## [1.6.0] - 2025-11-26

Major Features Added:

Added a trading journal. Entries are grouped by date and then further divided by symbols keep the notes specific to the
symbol making it easier to keep the thoughts focused and condense.

## [1.5.0] - 2025-11-24

Major Features Added:

- Added symbol maintenance in the toolbar. Users can now add symbols plus import and export to csv.
- Added symbol maintenance in the toolbar. Users can now add symbols plus import and export to csv.

Fixes included:

- Fixed Date display format for all tables and input boxes to be consistent throughout the app.
- Simplified some verbiage used in the stats overview section

## [1.4.0] - 2025-11-22

Major Features added:

- Added stop loss tab calculation.
- Updated add transaction feature to add more details for each transaction.
- Added edit transaction functionality
- Fixed tab display at bottom to be more consistent

-----

Tech related updates:

- Migrated the css file to a more Java orientated way.

## [1.3.0] - 2025-11-09

Major functionality update for daily preparations.

- Added tab for daily preparation section
- Daily symbol filtering is based on date range selected in the stats comp.
- Only 1 symbol can be added per 1 day. Symbols list is generated internally

## [1.2.1] - 2025-08-27

Minor bugfix. Fixed divide by zero issue causing calculation to fail.

## [1.2.0] - 2025-08-22

Major updated on visual elements and statics component.

- Added win rate for all trade formations.
- Internalized CSS style sheet and customized tabs and toolbar buttons.
- Reorganised the scene management and moved to tab system.