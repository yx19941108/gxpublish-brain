Owner: Codex
Task: editorial-manuscript-rebuild
Status: Snapshot Ready
Updated: 2026-03-17

# Official Documentation Knowledge Base

## Purpose

This directory stores a local snapshot of the official documentation sources that should be consulted before the next design-review session for the editorial manuscript rebuild.

This is a local knowledge base snapshot, not the long-term source of truth.

## Source Snapshot

### 1. RuoYi-Vue-Plus / plus-doc

- Official site:
  - `https://plus-doc.dromara.org/#/ruoyi-vue-plus/home`
- Local snapshot source:
  - `https://github.com/JavaLionLi/plus-doc.git`
- Local path:
  - `doc/official_doc/ruoyi-vue-plus-plus-doc/`
- Snapshot content:
  - root markdown navigation files
  - `common/`
  - `questions/`
  - `ruoyi-vue-plus/`
  - `plus-ui/`
  - `ruoyi-cloud-plus/`

### 2. Warm-Flow

- Official site entry:
  - `https://warm-flow.dromara.org/master/introduction/introduction.html`
- Direct shell fetch note:
  - the site page was reachable in browser-style web browsing, but direct shell HTTP fetch hit TLS/connection reset on this machine
- Upstream official fallback used for local snapshot:
  - `https://gitee.com/warm_4/warm-flow-doc.git`
- Evidence for fallback legitimacy:
  - upstream `warm-flow` official `README.md` explicitly points to `warm-flow-doc.git` as the local deployment documentation source
- Local path:
  - `doc/official_doc/warm-flow-doc/`
- Snapshot content:
  - `README.md`
  - `src/`

## Verified Entry Files

- RuoYi-Vue-Plus home:
  - `doc/official_doc/ruoyi-vue-plus-plus-doc/ruoyi-vue-plus/home.md`
- Warm-Flow introduction:
  - `doc/official_doc/warm-flow-doc/src/master/introduction/introduction.md`

## Snapshot Size

- `ruoyi-vue-plus-plus-doc`: `152` markdown files
- `warm-flow-doc`: `264` markdown files

## Usage Boundary

1. Use this directory as the local reading baseline for the next session.
2. If a conclusion depends on version-sensitive or newly updated framework behavior, re-open the official online source before finalizing.
3. Do not treat this snapshot as proof that the local project already implements the documented upstream behavior.

## Recommended Reading Start

1. `doc/official_doc/ruoyi-vue-plus-plus-doc/ruoyi-vue-plus/home.md`
2. `doc/official_doc/warm-flow-doc/src/master/introduction/introduction.md`
3. Then expand through the local sidebar-linked and section-linked child documents as needed.
