Ice Sheet

NHL fantasy hockey draft companion. Spring Boot backend, React frontend.

Backend module: backend/nhl-stats-api, base package com.rileywoytas.nhl_stats_api
Player and stats data comes from the public NHL API

Commands

Backend is a single-project Gradle build (no Maven); run these from backend/nhl-stats-api:
Test backend: ./gradlew test (one class: ./gradlew test --tests '*SnakeOrderTest')
Build backend (compile + test + jar): ./gradlew build
Run backend: ./gradlew bootRun (port 8080)
Don't run `bootRun` — it doesn't exit and will hang the session. Ask me to start
the server and report what happens.

Frontend dev server(run from frontend/nhl-stats-api): npm run dev


Working style

Build one layer at a time and verify it before moving to the next. Don't scaffold ahead into work that hasn't been designed yet.
When something in the existing code contradicts what I've asked for, say so instead of working around it silently.
Prefer small, reviewable changes. If a task touches more than a handful of files, outline the plan first.

Testing

Construct real domain objects in tests. Don't reach for Mockito.
Where a collaborator has to be stubbed, write a hand-rolled fake rather than when(...)/verify(...) chains.
Keep logic testable without Spring: pure classes with plain JUnit tests are preferred over @SpringBootTest wherever the logic allows it.
Favour unit tests over integration tests; reserve integration tests for wiring and persistence behaviour that unit tests genuinely can't cover.

Domain

The league is 16 teams, snake draft.
A DraftSession is either MOCK (CPU drafters make the other picks) or LIVE (every pick is recorded by hand on draft night). Both modes share one pick model — keep it that way.
Player valuation for CPU drafters: imported ADP/rankings CSV where available, last season's fantasy points otherwise.

Frontend conventions

Theme: "Scoreboard" — graphite background, amber accents, Space Mono.
Terminology is deliberate and shouldn't be normalised away:
"Season Stats", not "Season Snapshot"
"TOI/G", not "ATOI"
Goalie section titles use GS count, not GP
SKATERS is the default position filter; the ALL filter sits at the far right of the filter row.
