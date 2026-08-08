# FORM Public Roadmap

FORM is an independent research/side project. This roadmap describes public product and validation directions; it is not a promise of dates, clinical performance or future availability.

## Current public release — v0.9.x

The current public research preview focuses on:

- fast resistance-training session logging;
- editable 2–6 day program structures;
- RIR/load/rest/session-note capture;
- longitudinal adherence, PR, estimated-strength and workload views;
- exercise intelligence and substitution context;
- readiness/session-rescue/plateau decision-support experiments;
- privacy-first local operation with optional cloud functionality;
- optional reduced-data FORM Club publishing;
- export and deletion controls;
- transparent limitations and research-feedback intake.

## Path toward v1.0

### 1. Public/private architecture separation

- Move confidential backend, algorithms, schemas, internal research and architecture into a dedicated private source boundary.
- Keep only browser-required code, public documentation and distribution artifacts in the public repository.
- Move any genuinely proprietary computation behind authenticated APIs so confidential logic is not shipped to browsers.

### 2. Validation framework

- Define each output as deterministic calculation, estimate, proxy, heuristic, ranking or gamification score.
- Build reproducible synthetic and real-world test cases for deterministic calculations.
- Compare estimated-strength behavior against appropriate reference/test conditions rather than claiming one universal accuracy percentage.
- Track calibration/error by exercise, repetition range, experience level and data-history depth where sufficient data exists.
- Test recommendation stability and failure cases before making stronger predictive claims.
- Publish methodology and negative/limiting results where they are suitable for public release.

### 3. Data quality

- Detect impossible/implausible set entries and duplicate session records.
- Improve longitudinal comparability when exercises, machines, ranges of motion or units change.
- Make missing-data and low-history uncertainty visible in every dependent recommendation.

### 4. Training analytics

- Improve trend intervals and history comparison controls.
- Make workload views filterable by period, program, exercise and muscle group.
- Add explicit provenance/explanation for derived values.
- Preserve the distinction between workload proxies and biological outcomes.

### 5. Product usability

- Improve keyboard and screen-reader accessibility.
- Validate responsive behavior across major browser/device combinations.
- Reduce friction in session logging while retaining enough context for meaningful longitudinal analysis.
- Improve empty states, onboarding and recovery from malformed local data.

### 6. Privacy and security

- Continue owner-scoped authorization and least-data public projections.
- Add stronger automated checks against accidental secret exposure.
- Verify account export/deletion end-to-end on configured cloud deployments.
- Document retention and infrastructure boundaries when cloud services are enabled.

### 7. Distribution

- Maintain tagged GitHub Releases with checksummed public artifacts.
- Maintain a reproducible GHCR container for the public web surface.
- Preserve citation metadata, changelog and release notes.
- Keep live GitHub Pages as the fastest evaluation path.

## Research questions worth testing

FORM is particularly interested in evidence or reproducible evaluation around:

- how much history is needed before load/progression guidance becomes meaningfully more stable;
- when estimated-strength trends become misleading across rep ranges or exercise changes;
- how reliable self-reported RIR/readiness context is across populations and exercises;
- whether time-constrained session compression preserves adherence better than simply skipping sessions;
- how users interpret workload proxies and whether the interface adequately prevents overclaiming;
- whether transparency around uncertainty changes user decisions compared with opaque recommendation scores.

## Contribute evidence

Use the repository's **Research / Validation Feedback** issue form for evidence-backed critiques, replication ideas, papers or proposed tests.

Live product: https://dharan1007.github.io/trakiler/

Contact: dharan.poduvu@gmail.com
