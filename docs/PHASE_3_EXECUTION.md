# Phase 3 Execution Plan

## Workflow

Every task follows:

PLAN
→ HUMAN REVIEW
→ IMPLEMENT
→ REVIEW
→ FIX
→ MANUAL CHECK
→ COMMIT

Use a new Cursor Chat for each major task.

Do not ask Cursor to implement multiple tasks together.

---

# Task Order

01 Database & Flyway Consolidation (implemented: `V1__phase3_baseline.sql`, archive `db/archive/phase1-phase2`, tag `pre-phase3-flyway-v35`)
02 Architecture Audit
03 Test Coverage Audit & Safety Net
04 Level 1–10 Player Journey Design
05 Level 1–10 Progression & Economy Design
06 Quest & NPC Framework
07 Level 1–10 Content Pass
08 Combat Refinement
09 Arena Training Grounds
10 Early PvE & Dungeon Refinement
11 Level 1–10 Balance Validation
12 Release-Quality UI Design System
13 Main Shell & Office Mode
14 Existing Feature Productization
15 Clan Foundation
16 Player Identity & Social Presence
17 Onboarding & FTUE
18 Activity / Notifications / Return Experience
19 Telemetry & Diagnostics
20 Production Hardening
21 Level 1–10 Full Flow Audit
22 Final Integration & Release Readiness

---

# Recommended Branches

phase3/01-db-baseline
phase3/02-architecture-audit
phase3/03-test-safety-net
phase3/04-player-journey
phase3/05-progression-economy
phase3/06-quests
phase3/07-early-content
phase3/08-combat-refinement
phase3/09-arena-training
phase3/10-pve-dungeon
phase3/11-balance-validation
phase3/12-design-system
phase3/13-shell-office
phase3/14-productization
phase3/15-clans
phase3/16-player-social
phase3/17-onboarding
phase3/18-return-experience
phase3/19-telemetry
phase3/20-production-hardening
phase3/21-flow-audit
phase3/22-final-integration

---

# Suggested Tags

After Task 03:

phase3-technical-foundation

After Task 11:

phase3-gameplay-core

After Task 16:

phase3-social-ui-alpha

After Task 22:

phase3-complete