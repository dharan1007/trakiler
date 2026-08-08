# FORM — Training System

FORM is a free, private-first workout tracker and training-analysis side project.

Live: `https://dharan1007.github.io/trakiler/`

## Product

FORM has six focused surfaces:

- **Today** — weights, reps, RIR, rest and workout notes.
- **Programs** — editable 2–6 day templates and goal-aware suggestions.
- **Progress** — adherence, weekly streaks, PRs, estimated-strength trends, weekly workload, bodyweight and measurements.
- **Exercises** — exercise scores, benefits, heat maps, alternatives and tutorial searches.
- **Club** — optional public daily leaderboard; demo rows are clearly labeled and unranked.
- **Profile** — goal, experience, schedule, privacy, export, cloud recovery and deletion.

## Progression

Exercise-specific estimated strength uses a bounded Epley-style estimate:

```text
e1RM = weight × (1 + min(reps, 12) / 30)
```

FORM uses repeated logged performance for load guidance rather than hardcoded universal weights. Muscle workload is a transparent programming proxy (primary muscle = 1 set, secondary = 0.5), not a direct measurement of muscle growth.

## Programs

Current families include Full Body Minimum Effective, Starter/Full Body, Recomposition, Upper/Lower, Hypertrophy Upper/Lower, General Strength, Aesthetic/Hypertrophy 5-Day, Aesthetic 6-Day and PPL 6-Day. Matching considers goal, experience, training days and expected session duration. All templates remain editable.

## Backend and privacy

`supabase/schema.sql` defines RLS-protected tables for profiles, legal acceptance, workouts, sets, body logs, measurements and daily activity, plus a reduced opt-in public leaderboard projection. Club score/streak calculations are server-side. `supabase/functions/delete-account/index.ts` implements protected cloud-account deletion.

Exact bodyweight, measurements, email, notes and private set history are not public Club fields. Without a configured backend, FORM stays local-first and clearly reports that global publishing is unavailable.

Only a public Supabase URL and publishable/anon-compatible key belong in `club-config.js`. Never expose a service-role key in browser code.

## User controls

FORM includes minimal onboarding, optional anonymous identity, optional magic-link recovery, public-Club opt-in, JSON export, local deletion, cloud deletion, [`privacy.html`](./privacy.html) and [`terms.html`](./terms.html).

## Verification

`.github/workflows/verify.yml` parses the browser JavaScript and verifies required app/legal/privacy files and critical RLS markers before release.

## License

Original FORM source is released with public-domain / Unlicense intent. See [`LICENSE`](./LICENSE). Third-party services and linked material retain their own rights.

## Safety

FORM is an educational workout log, not medical advice or a medical device. Exercise scores, strength estimates, heat maps, suggestions and workload values are context-dependent approximations.

`VERA_OneDay_Colab.ipynb` is a separate research artifact and is not part of FORM runtime behavior.
