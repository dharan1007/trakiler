# FORM — Training System

FORM is a free, private-first workout tracker and training-analysis side project. It combines editable programs, detailed set logging, exercise intelligence, progression analytics, optional body tracking and an opt-in social leaderboard without requiring wearables or health-device integrations.

Live site: `https://dharan1007.github.io/trakiler/`

## Product structure

FORM uses six focused surfaces:

1. **Today** — current workout, loads, reps, RIR, rest timer and notes.
2. **Programs** — editable templates plus profile-aware suggestions.
3. **Progress** — adherence, streaks, PRs, exercise trends, weekly workload, bodyweight and measurements.
4. **Exercises** — searchable exercise library with FORM score, benefits, body heat maps, alternatives and tutorial searches.
5. **Club** — optional global daily activity ranking with clearly labeled demo data when the real board is sparse.
6. **Profile** — goals, experience, weekly schedule, privacy, cloud recovery, export and deletion.

## Workout and load tracking

Each working set can record weight, repetitions, RIR, completion state and rest. Completing a set can start the rest timer automatically. Session history stores elapsed time, total/completed sets, total volume, notes and set data.

When history exists, FORM estimates exercise-specific strength using a bounded Epley-style estimate:

```text
e1RM = weight × (1 + min(reps, 12) / 30)
```

The next-load hint is derived from prior performance and the current target rep range; it is not a tested 1RM.

## Progress analytics

Progress includes current-week adherence, weekly consistency streaks, best streak, exercise PRs, latest-session PR detection, repeated-exposure e1RM trends, 7-day volume, weighted muscle workload, optional bodyweight/body-fat logs, optional circumference measurements and conservative adjustment suggestions.

Primary muscles count as one weighted set and secondary muscles as 0.5 for the workload view. This is a programming proxy, not a direct measurement of muscle growth or EMG activity.

## Programs

Current program families include:

- Full Body 2-Day Minimum Effective
- Starter 3-Day Full Body
- Full Body 3-Day
- Recomposition 3-Day
- Upper / Lower 4-Day
- Hypertrophy 4-Day Upper / Lower
- General Strength 4-Day
- Aesthetic 5-Day
- Hypertrophy 5-Day Balanced
- Aesthetic 6-Day Specialization
- Push / Pull / Legs 6-Day

Program matching considers goal, experience, available training days and approximate session duration. Templates remain editable.

## Exercise intelligence

`data.js` contains the structured exercise library: category, primary/secondary muscles, equipment, FORM score and rationale, benefits, cues, rep range, RIR, rest, load guidance, alternatives, heat-map information and three YouTube search routes per movement.

YouTube links open public search results; FORM does not host or redistribute creators' videos.

## Club and privacy

With a dedicated Supabase backend, authenticated users can opt in to public Club ranking. The public projection is limited to display name, daily score, completed sets, manually entered steps, manually entered active minutes, streak and total workout volume.

Exact bodyweight, circumference measurements, email, workout notes and private set history are not in the public leaderboard projection.

If no backend is connected, demo athletes are clearly labeled **demo** and do not receive a real rank. A local unpublished row is labeled as a local preview.

## Backend

The optional cloud layer is implemented for Supabase/PostgreSQL:

```text
Auth
├── form_profiles
├── form_legal_acceptance
├── form_workouts
├── form_workout_sets
├── form_body_logs
├── form_measurements
├── form_daily_activity
└── form_public_daily_leaderboard
```

`supabase/schema.sql` defines the schema, constraints, RLS policies, server-calculated Club score/streak and public leaderboard projection. `supabase/functions/delete-account/index.ts` implements protected account deletion.

Never place a service-role/secret key in browser-delivered files.

## Local-first controls

FORM works without a cloud account and includes minimal onboarding, optional anonymous cloud identity, optional email magic-link recovery, opt-in Club publication, JSON export, local deletion, cloud deletion, a Privacy Policy and Terms of Use.

See [`privacy.html`](./privacy.html) and [`terms.html`](./terms.html).

## Connecting Supabase

Use a dedicated FORM project rather than an unrelated production database:

1. Create the project.
2. Apply `supabase/schema.sql`.
3. Enable Anonymous Sign-Ins if desired.
4. Deploy `supabase/functions/delete-account/index.ts` with JWT verification enabled.
5. Run Supabase security/performance advisors.
6. Add only the project URL and public publishable/anon-compatible key to `club-config.js`.

```js
window.FORM_SUPABASE = {
  url: "https://YOUR_PROJECT.supabase.co",
  key: "YOUR_PUBLIC_PUBLISHABLE_OR_ANON_KEY"
};
```

Until that is done, the live site remains deliberately local-first and explicitly reports that global publishing is unavailable.

## Verification

`.github/workflows/verify.yml` checks JavaScript syntax, required Progress/Profile/legal surfaces, private-first schema markers, absence of a service-role key in browser assets and required runtime files.

## Deployment

The production static bundle is published from the root of the legacy `gh-pages` branch.

## License

Original FORM source code is released with public-domain / Unlicense intent. See [`LICENSE`](./LICENSE). Third-party videos, services, APIs, trademarks and datasets retain their own rights.

## Safety

FORM is a training log and educational project, not medical advice or a medical device. Exercise scores, estimated strength, suggestions, heat maps and workload counts are context-dependent approximations.

`VERA_OneDay_Colab.ipynb` remains a separate research artifact and is not part of the FORM runtime.
