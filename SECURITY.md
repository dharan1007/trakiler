# FORM Security and Source Boundary

FORM is a public-facing research/side project with a privacy-first product direction. This document explains what is safe to publish, what must remain private, and how to report security or privacy concerns.

## Reporting a security or privacy issue

Contact: **dharan.poduvu@gmail.com**

Please include:

- a concise description of the issue;
- affected page, feature or endpoint;
- reproducible steps where appropriate;
- the impact you believe is possible;
- a minimal proof of concept if one is needed to demonstrate the issue.

Do **not** send passwords, authentication tokens, private medical information, identity documents or unrelated personal data.

## Public/private source model

FORM's target repository model is:

### Public

- public product documentation;
- the live demo entry surface;
- privacy and terms pages;
- static deployment artifacts that must be delivered to a browser;
- non-sensitive UI assets;
- high-level feature and data-boundary documentation;
- security reporting instructions.

### Private

- backend implementation;
- database schemas and migrations;
- privileged authorization/policy implementation;
- internal API implementation;
- detailed scoring coefficients;
- proprietary recommendation and decision-engine logic;
- private architecture/deployment topology;
- internal research notebooks and experiments;
- private datasets/test fixtures;
- credentials and secrets.

## Browser-delivered code is not secret

Any JavaScript, HTML, CSS or configuration delivered to a user's browser must be treated as inspectable. Minification, bundling or obfuscation is **not a security boundary**.

If an algorithm must remain confidential, it should execute behind a private server/API. The public client should receive only the minimum interface and result needed for the feature.

## Credentials

The public browser bundle must never contain:

- Supabase service-role keys;
- database passwords;
- private API keys;
- signing secrets;
- OAuth client secrets intended to be confidential;
- private encryption keys;
- provider administrative credentials.

A Supabase publishable/anon-compatible client key may be browser-delivered only when the backend is designed for that model and Row Level Security/authorization correctly protects private data.

If a true secret is ever committed publicly, **revoke or rotate it first**. Removing the current file is not sufficient because Git history, forks, clones or cached references may retain earlier content.

## User-data boundary

Private-by-default records can include:

- workout history and individual sets;
- notes;
- bodyweight/body-fat entries;
- circumference measurements;
- readiness answers;
- training constraints/preferences;
- email used for optional recovery;
- private profile/account data.

The optional public Club layer should contain only an explicitly reduced, opt-in set of daily aggregate fields. Exact bodyweight, detailed sets, notes, measurements, readiness answers, constraints and email must not be included in the public leaderboard projection.

## Authorization requirements

For any cloud deployment:

- private records must be owner scoped;
- client input must not be trusted as authorization;
- privileged calculations and writes should be server-authoritative where integrity matters;
- public views should be reduced projections, not direct exposure of private tables;
- account deletion must authenticate the caller and remove the appropriate account-scoped records;
- logging must avoid leaking secrets or unnecessary private data.

## Public-source migration warning

Historically, this repository has contained client implementation, backend/schema material, algorithms and a research notebook in a public Git history. Simply deleting those paths from `main` does not make historical copies private.

A proper hardening migration should:

1. Create a dedicated **private source repository**.
2. Move backend, schema, algorithms, internal architecture and research assets into that private repository.
3. Refactor confidential algorithms so they do not need to be shipped to the browser.
4. Keep only the minimum public deploy artifacts/documentation here.
5. Audit all historical commits for actual credentials or sensitive data.
6. Rotate any credential that was ever public.
7. If sensitive material must be purged from history, use a coordinated history-rewrite procedure such as `git filter-repo` and account for forks/clones/cached references.
8. Re-verify the deployed GitHub Pages artifact after the split.

## Security is not a fitness guarantee

FORM's security controls protect software/data boundaries; they do not validate the scientific or medical correctness of training suggestions. See the project README, Privacy Policy and Terms for those limitations.
