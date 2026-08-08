# FORM — Training System

FORM is a free, private-first workout tracker and training-analysis side project. It combines editable programs, detailed set logging, exercise intelligence, progression analytics, optional body tracking and an opt-in social leaderboard without requiring wearables or health-device integrations.

Live: `https://dharan1007.github.io/trakiler/`

## Main surfaces

- **Today** — workout, weights, reps, RIR, rest timer and notes.
- **Programs** — editable templates and goal-aware suggestions.
- **Progress** — adherence, streaks, PRs, exercise trends, workload, bodyweight and measurements.
- **Exercises** — searchable library, FORM scores, benefits, heat maps, alternatives and tutorials.
- **Club** — optional global daily leaderboard; demo rows are explicitly labeled and unranked.
- **Profile** — goal/experience/schedule, privacy, cloud recovery, export and deletion.

## Progression

FORM records individual sets and uses repeated history rather than fixed universal weights. Exercise-specific estimated strength uses:

```text
e1RM = weight × (1 + min(reps, 12) / 30)
```

Progress includes weekly adherence, weekly on-target streaks, best streak, exercise PRs, latest-session PR detection, e1RM trends, 7-day volume, weighted muscle workload, optional bodyweight/body-fat logs, optional circumference measurements and conservative suggestions.

Muscle workload is deliberately a programming proxy: primary muscles count as 1 set and secondary muscles as 0.5. It is not a direct measurement of hypertrophy or EMG activity.

## Programs

Included program families range from 2 to 6 days per week: Full Body Minimum Effective, Starter/Full Body, Recomposition, Upper/Lower, Hypertrophy Upper/Lower, General Strength, 5-Day Hypertrophy/Aesthetic, 6-Day Aesthetic and PPL. Matching considers goal, experience, days available and session duration. Every template remains editable.

## Exercise library

Exercises can define muscles, equipment, FORM score/rationale, benefits, cues, target reps/RIR/rest, load guidance, alternatives, body-map emphasis and three YouTube search routes. YouTube content is linked, not copied or redistributed.

## Privacy and Club

The optional Supabase backend keeps private user records separate from the opt-in public Club projection. Public Club fields are limited to display name and daily aggregate ranking fields. Exact bodyweight, measurements, email, notes and private set history are not public leaderboard fields.

Without a configured backend, the application remains local-first. Demo rows are labeled `demo` and have no real rank.

## Backend

`supabase/schema.sql` defines RLS-protected tables for profile, legal acceptance, workouts, sets, body logs, measurements and daily activity, plus a reduced public leaderboard projection. Club score/streak calculations are server-side. `supabase/functions/delete-account/index.ts` performs protected account deletion.

Connect a dedicated project by placing only its public URL and publishable/anon-compatible key in `club-config.js`. Never expose a service-role key in browser code.

## User controls

FORM includes minimal onboarding, optional anonymous cloud identity, optional magic-link recovery, explicit public-Club opt-in, JSON export, local deletion, cloud account deletion, [`privacy.html`](./privacy.html), and [`terms.html`](./terms.html).

## Verification and deployment

`.github/workflows/verify.yml` parses all browser JavaScript and verifies required app/legal/privacy files and key RLS markers. Production is deployed from the root of `gh-pages`.

## License

Original FORM source is released with public-domain / Unlicense intent; see [`LICENSE`](./LICENSE). Third-party services and linked material retain their own rights.

## Safety

FORM is a workout log and educational side project, not medical advice or a medical device. Scores, estimated strength, suggestions, heat maps and workload are context-dependent approximations.

`VERA_OneDay_Colab.ipynb` is a separate research artifact and is not part of FORM runtime behavior.
