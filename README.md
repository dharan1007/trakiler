# FORM — Training System

FORM is a free, private-first workout tracker and training-analysis side project. It combines editable programs, detailed set logging, exercise intelligence, progression analytics, optional body tracking and an opt-in social leaderboard without requiring wearables or health-device integrations.

Live site: `https://dharan1007.github.io/trakiler/`

## Product structure

FORM deliberately uses six simple surfaces instead of putting every metric on the workout screen:

1. **Today** — current workout, loads, reps, RIR, rest timer and notes.
2. **Programs** — editable templates plus profile-aware suggestions.
3. **Progress** — adherence, streaks, PRs, exercise trends, weekly workload, bodyweight and measurements.
4. **Exercises** — searchable exercise library with FORM score, benefits, body heat maps, alternatives and tutorial searches.
5. **Club** — optional global daily activity ranking with clearly labeled demo data when the real board is sparse.
6. **Profile** — goals, experience, weekly schedule, privacy, cloud recovery, export and deletion.

## Workout logging

Each working set can record:

- weight
- repetitions
- RIR
- completion state
- prescribed rest period

Completing a set can start the rest timer automatically. Session history stores elapsed time, total/completed sets, total volume, notes and individual set data.

### Load guidance

FORM does not invent a universal kg recommendation. When previous set history is available, it estimates exercise-specific strength using a bounded Epley-style estimate:

```text
e1RM = weight × (1 + min(reps, 12) / 30)
```

The next-load hint is then derived from the user's own prior performance and current target rep range. It is a training estimate, not a tested 1RM.

## Progress analytics

The Progress surface adds the tracker features missing from the original single-page version:

- current-week adherence against the user's chosen training frequency
- weekly consistency streak and best streak
- personal records by exercise-specific estimated 1RM
- new-PR detection from the latest completed session
- repeated-exposure e1RM trends
- 7-day training volume
- weighted muscle workload based on logged sets
- optional bodyweight logs
- optional body-fat percentage entry
- optional circumference measurements
- trend-aware suggestions for adherence, plateaus, workload and bodyweight context

Muscle workload intentionally uses a transparent approximation: primary muscles count as one weighted set and secondary muscles as 0.5. It is a programming/workload proxy, **not** a measurement of hypertrophy, tissue size or EMG activity.

## Programs

FORM includes the original editable templates plus a goal-aware v3 layer. Current program families include:

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

Program matching considers:

- primary goal: general fitness, muscle gain, strength or recomposition
- experience: beginner, intermediate or advanced
- available training days
- approximate session duration

Templates remain editable. The matcher is a starting-point heuristic, not a medical or individualized coaching prescription.

## Exercise intelligence

`data.js` contains the structured exercise library. Exercise objects can include:

- category
- primary and secondary muscles
- equipment
- FORM score
- score rationale
- hypertrophy/strength/programming benefits
- execution cues
- rep range
- target RIR
- rest duration
- load guidance
- alternative exercises
- body-map heat information
- three YouTube search routes for independent technique demonstrations

YouTube links open public search results. FORM does not copy, host or redistribute creators' videos.

## FORM Club

Club has two explicit modes:

### Real backend mode

When a dedicated Supabase backend is configured, authenticated users can opt in to a public Club profile and publish a daily aggregate. The public projection can expose:

- display name
- daily FORM score
- completed sets
- manually entered steps
- manually entered active minutes
- streak
- total workout volume

Exact bodyweight, body measurements, email, workout notes and private set history are not part of the public leaderboard table.

### Demo/local preview

If no backend is connected, or if the real board is sparse, demo athletes can appear only as an empty-state aid. Demo rows are explicitly marked **demo** and do not receive a real rank. Local unpublished activity is explicitly marked as a local preview.

## Backend architecture

The optional cloud layer is implemented for Supabase/PostgreSQL.

```text
browser
  ├─ localStorage — local-first workout/product state
  └─ Supabase client
       ├─ Auth — anonymous pseudonymous identity + optional email magic link
       ├─ form_profiles
       ├─ form_legal_acceptance
       ├─ form_workouts
       ├─ form_workout_sets
       ├─ form_body_logs
       ├─ form_measurements
       ├─ form_daily_activity
       └─ form_public_daily_leaderboard
```

`supabase/schema.sql` contains the database definition and RLS policies.

Private tables use authenticated-user ownership policies. Club scoring is recalculated in PostgreSQL rather than trusting a score sent by the browser. The public leaderboard is a reduced projection separate from the user's private activity record.

### Account deletion

`supabase/functions/delete-account/index.ts` implements the server-side deletion operation. It validates the bearer session, requires an explicit destructive confirmation string and uses the server-only service credential to delete the authenticated identity. FORM-owned tables reference `auth.users` with cascading deletion.

Never place a Supabase service-role/secret key in `club-config.js`, `backend.js`, HTML or any other browser-delivered file.

## Local-first privacy model

The application works without a cloud account. Local data stays in the browser until the user deletes it, clears site storage or opts into a configured cloud-backed feature.

The current interface includes:

- onboarding with minimal training-profile fields
- optional anonymous cloud identity
- optional email magic-link recovery
- opt-in public Club profile
- full local/cloud JSON export
- separate local data deletion
- cloud account deletion
- Privacy Policy
- Terms of Use

See [`privacy.html`](./privacy.html) and [`terms.html`](./terms.html).

## Supabase setup

A dedicated FORM Supabase project is recommended; do not mix this hobby application's user data into an unrelated production project.

1. Create a dedicated project.
2. Apply `supabase/schema.sql`.
3. Enable Anonymous Sign-Ins if pseudonymous account creation is desired.
4. Deploy `supabase/functions/delete-account/index.ts` with JWT verification enabled.
5. Run Supabase security/performance advisors and resolve applicable findings.
6. Put only the project URL and **publishable/anon-compatible public key** in `club-config.js`.
7. Never commit the service-role key.

Example browser config:

```js
window.FORM_SUPABASE = {
  url: "https://YOUR_PROJECT.supabase.co",
  key: "YOUR_PUBLIC_PUBLISHABLE_OR_ANON_KEY"
};
```

Without this configuration the app remains functional in local-first mode and Club clearly reports that global publishing is unavailable.

## Files

```text
trakiler/
├── index.html
├── styles.css
├── v3.css
├── data.js
├── programs-v3.js
├── app.js
├── insights.js
├── backend.js
├── club.js
├── club-config.js
├── privacy.html
├── terms.html
├── LICENSE
├── supabase/
│   ├── schema.sql
│   └── functions/
│       └── delete-account/
│           └── index.ts
├── .github/workflows/verify.yml
└── VERA_OneDay_Colab.ipynb
```

`VERA_OneDay_Colab.ipynb` remains a separate research artifact and is not part of FORM runtime behavior.

## Verification

The GitHub Actions workflow checks:

- JavaScript syntax for every browser module
- presence of Progress, Profile and onboarding surfaces
- legal pages and license marker
- key private-first/RLS schema elements
- absence of a service-role credential name in browser-delivered assets
- presence of all local runtime assets referenced by the application

## Deployment

The repository currently uses legacy GitHub Pages branch deployment. The production bundle is published from the root of `gh-pages`.

## License

The repository's original FORM source code is released with a public-domain / Unlicense intent. See [`LICENSE`](./LICENSE). The license does not grant rights to third-party videos, services, APIs, trademarks, datasets or other linked material.

## Safety and scope

FORM is a training log and educational side project, not medical advice or a medical device. Exercise rankings, estimated strength, workout suggestions, heat maps and workload counts are context-dependent approximations. Users should choose exercises and loading appropriate to their own abilities and seek qualified professional care when needed.
