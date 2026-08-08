# FORM — Modular Training System

FORM is a clean browser-based workout tracker, editable training-template system, exercise-intelligence library and optional social activity club.

The current application has been rebuilt from the original single-file tracker into separate data, presentation and runtime modules so exercise definitions, programs, ranking logic, Club connectivity and UI code can evolve independently.

The repository also still contains `VERA_OneDay_Colab.ipynb`, an unrelated experimental language-model research notebook. FORM is the deployed browser application.

## Live application

GitHub Pages is configured from the repository's `gh-pages` branch.

Production URL:

`https://dharan1007.github.io/trakiler/`

## Product structure

FORM deliberately has four primary surfaces rather than one overloaded dashboard.

### Today

The active workout contains:

- program/day selector
- session timer
- set completion percentage
- completed training volume
- completed working-set count
- next-load hint
- editable load / reps / RIR for each set
- automatic rest timer
- exercise information button
- session notes
- workout history
- JSON export
- native share/clipboard sharing

### Programs

FORM ships with editable templates:

- Aesthetic 6-Day Specialization
- Upper / Lower 4-Day
- Full Body 3-Day
- Push / Pull / Legs 6-Day
- Aesthetic 5-Day
- Starter 3-Day

A template can be selected without rewriting the UI. The active day can then be edited directly:

- day name
- focus
- exercise selection
- exercise order
- sets
- rep range
- RIR target
- rest duration

The "Create custom" action clones the active program so the user can modify their own version without mutating the source template in `data.js`.

## Exercise intelligence

The exercise library in `data.js` currently includes machine, cable, dumbbell and bodyweight options across:

- side / front / rear delts
- upper and mid chest
- lats
- upper back
- biceps
- triceps
- quadriceps
- hamstrings
- glutes
- calves
- abdominal training

Each exercise definition contains structured fields rather than UI-specific markup:

```js
{
  name,
  category,
  equipment,
  score,
  primary,
  secondary,
  benefits,
  why,
  cues,
  reps,
  rir,
  rest,
  load,
  alternatives,
  heat
}
```

This is why a new exercise can be added to the library without creating a new HTML component.

## The exercise `i` drawer

Every active-workout exercise and library entry exposes an information drawer containing:

- FORM score
- equipment
- ranking explanation
- front/back body heat maps
- primary/secondary muscle emphasis
- practical benefits
- execution cues
- recommended rep range
- effort / RIR guidance
- rest guidance
- loading logic
- alternative movements
- three independent YouTube tutorial searches
- FORM ranking methodology

### YouTube tutorial strategy

FORM does not copy, host or redistribute exercise creators' video files.

For every exercise it creates three public YouTube search routes:

1. Renaissance Periodization + exercise + technique
2. Jeff Nippard + exercise + form
3. Muscle & Strength + exercise + exercise guide

This provides multiple free-to-access discovery routes and remains more resilient than hard-coding a single video ID that can later be deleted, made private or replaced. The links are references to YouTube search results; they are not a claim that the underlying videos are licensed for redistribution.

## FORM exercise score

FORM scores are programming aids, not universal biological rankings.

The score combines qualitative assessment of:

- stability
- target-muscle tension
- usable range of motion
- progression simplicity
- stimulus-to-fatigue characteristics
- setup practicality
- technique repeatability
- evidence confidence

The reason for each score is shown to the user rather than hiding the number behind an unexplained algorithm.

An exercise that scores slightly lower can still be the better choice for an individual because of anatomy, pain history, equipment availability, preference, skill or a specific training goal.

## Body heat maps

The information drawer generates front and back SVG body maps from each exercise's `heat` object.

Example:

```js
heat: {
  sideDelts: 1,
  frontDelts: 0.2
}
```

The renderer converts the relative muscle weights into low / medium / high visual emphasis classes.

The map is intentionally an exercise-emphasis visualization, not an anatomical EMG measurement or medical image.

## Loading and weights

FORM does not invent fixed kilogram prescriptions for unknown users.

Instead it records the user's own completed weight/repetition history and estimates exercise-specific strength with a bounded Epley-style estimated 1RM:

```text
e1RM = weight × (1 + min(reps, 12) / 30)
```

A target percentage is then selected from the requested rep range. Lower-repetition ranges receive a higher percentage; higher-repetition ranges receive a lower percentage.

The suggested load is rounded to practical increments and displayed as a next-set hint.

For a new exercise with no personal history, FORM shows the target percentage and asks the lifter to establish a technically valid baseline instead of fabricating an exact weight.

## Effort model

The templates generally use:

- compounds: approximately 1–3 RIR
- controlled isolation exercises: approximately 1–2 RIR
- optional very low RIR only where technique and exercise safety remain stable

RIR is editable per set and stored in the workout history.

## Workout persistence

FORM currently uses browser `localStorage` for private workout state and history.

Saved workout records contain:

- program
- day
- completion timestamp
- elapsed session time
- completed / planned set counts
- total volume
- session notes
- exercise ID
- exercise name
- load
- reps
- RIR

The Export action downloads the complete local state, history and Club activity as JSON.

## FORM Club

The Club surface ranks daily activity using a transparent capped score rather than a hidden social-feed algorithm.

Current scoring components are:

```text
score = min(sets, 40) × 4
      + min(volume / max(bodyweight, 40), 220) × 0.55
      + min(active_minutes, 180) × 0.55
      + min(steps / 1000, 25) × 3
      + log2(min(streak, 365) + 1) × 12
```

The caps prevent a single extreme input from growing without bound.

The daily table can expose:

- rank
- display name
- activity score
- completed working sets
- steps
- active minutes
- streak
- total logged training volume

### Local mode

Without a backend, Club works as a private local demonstration and can include the user's current daily activity alongside sample leaderboard entries.

No local-mode activity leaves the browser.

### Global mode

`supabase/schema.sql` contains a dedicated Supabase/Postgres implementation for global FORM Club.

It defines:

- `form_profiles`
- `form_daily_activity`
- `form_public_daily_leaderboard`
- explicit Data API grants
- RLS on exposed tables
- owner-only profile/activity policies
- authenticated leaderboard reads
- server-side activity-score calculation
- an authenticated publication trigger that copies only leaderboard-safe fields into the public ranking table

The schema deliberately computes the authoritative score on the server. A client-supplied `activity_score` cannot become the source of truth.

`club-config.js` contains only public client configuration placeholders. Never place a Supabase secret/service-role key in this file.

A dedicated Supabase project must be provisioned and the schema applied before Global mode is enabled.

## Security model for Club

The provided schema follows these principles:

- `user_id` must match `auth.uid()`
- profile/activity rows are owner-scoped
- the leaderboard receives a deliberately reduced public projection
- server-side triggers calculate score
- privileged publisher code lives in a non-public schema
- execution is revoked from `PUBLIC`
- exposed tables have RLS enabled
- public frontend configuration may contain only a Supabase publishable/anon-compatible client key, never a secret/service-role credential

Anonymous Supabase identities can be used for low-friction pseudonymous Club participation if Anonymous Sign-Ins are enabled for the dedicated project.

## Files

```text
trakiler/
├── index.html                  # FORM application shell
├── styles.css                 # responsive minimal design system
├── data.js                    # exercise library + program templates
├── app.js                     # workout, editor, heat-map and history runtime
├── club.js                    # local/global Club adapter
├── club-config.js             # public backend config placeholder
├── supabase/
│   └── schema.sql             # optional global Club backend
├── .nojekyll
├── README.md
└── VERA_OneDay_Colab.ipynb    # separate research artifact
```

The deployment branch contains only the static FORM site files required by GitHub Pages.

## Editing exercises

Add or modify entries in `data.js` under `FORM_DATA.exercises`.

Program templates reference exercises by stable ID, for example:

```js
["incline-smith", 4, "6–10", 2, 180]
```

meaning:

```text
exercise = incline-smith
sets     = 4
reps     = 6–10
RIR      = 2
rest     = 180 seconds
```

## Editing programs

Templates live under `FORM_DATA.templates` and contain day objects with `name`, `focus` and `items`.

The runtime works from these objects rather than hard-coded six-day assumptions, so three-, four-, five- and six-day programs use the same renderer.

## Deployment

This repository's Pages configuration is legacy branch-based deployment:

```text
source branch: gh-pages
path: /
```

The production `gh-pages` branch is generated from the static FORM application assets.

## VERA notebook

`VERA_OneDay_Colab.ipynb` is retained as an independent experimental architecture notebook. Claims written inside an experimental notebook should be treated as hypotheses or intended benchmark goals unless actual reproducible benchmark outputs are present.

VERA is not part of the FORM website runtime.

## Health and safety

FORM is a training-log and educational system rather than medical guidance.

Stop exercise and obtain appropriate assessment for symptoms such as sharp pain, chest pressure, fainting, unusual breathlessness, acute neurological symptoms or an obvious injury. Exercise selection, range of motion and training load should be adapted to the individual rather than forced to match a generic template.
