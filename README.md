# Trakiler

This repository currently contains **two separate experimental artifacts** rather than one unified application:

1. **FORM — Aesthetic Workout Tracker**: a standalone single-file browser workout tracker in `index.html`.
2. **VERA — Verified Expert Recurrent Architecture**: an experimental Google Colab notebook in `VERA_OneDay_Colab.ipynb` exploring a verifier-assisted language-model architecture.

There is no package manager, backend service, database, or build system in the current repository.

## Repository structure

```text
trakiler/
├── .nojekyll
├── index.html
├── VERA_OneDay_Colab.ipynb
└── README.md
```

## 1. FORM — Aesthetic Workout Tracker

`index.html` is a complete self-contained workout application: HTML, CSS and JavaScript are embedded in a single file.

The page is titled:

```text
FORM — Aesthetic Workout Tracker
```

and presents itself as a six-day specialization training system.

### Implemented training features

The browser application includes:

- six-day training navigation
- day-specific exercise programs
- exercise technique guides
- set completion checkboxes
- editable set data
- session clock
- per-set rest timer
- rest-timer adjustment controls
- completion percentage
- completed-set count
- logged training volume
- session notes
- completed-workout history summary
- export/reset controls
- persistent local browser data
- responsive mobile layout

The footer explicitly states that training data is stored only in the browser.

### Program structure

The current program is oriented around aesthetic hypertrophy specialization. Visible day themes include:

1. shoulder width / upper chest / abs
2. back width / biceps
3. chest / triceps / side delts / abs
4. back thickness / rear delts / abs
5. shoulders / arms / chest top-up
6. legs / abs

Each exercise record defines:

- exercise name
- number of work sets
- rep target
- effort/RIR target
- rest duration
- technique cue

The sixth day intentionally uses leg work without squat/deadlift dependence in the checked-in program.

### Training-state model

The application is client-only. State is managed in browser JavaScript and persisted locally rather than through an account/server.

That has useful privacy/offline properties but also means:

- progress does not automatically sync between devices;
- clearing browser storage can remove history;
- there is no server backup;
- data integrity depends on the browser/device;
- multiple users on the same browser profile are not isolated by account.

### Run FORM

No installation is required. Open:

```text
index.html
```

in a modern browser.

For a local HTTP origin instead of `file://`, run:

```bash
python -m http.server 8000
```

and visit:

```text
http://localhost:8000
```

### Static hosting

`.nojekyll` makes the repository suitable for direct static publishing such as GitHub Pages without Jekyll processing.

The tracker can also be served from Cloudflare Pages, Netlify, Vercel static hosting, or any basic web server.

## Health/safety scope

FORM contains exercise guidance but is software, not individualized medical care. The current UI itself instructs users to stop for warning symptoms such as sharp pain, chest pressure, faintness, unusual breathlessness, or neurological symptoms.

Training prescriptions should be adjusted for injury history, medical conditions, experience, equipment and recovery instead of treating the checked-in six-day program as universally appropriate.

## 2. VERA — Verified Expert Recurrent Architecture

`VERA_OneDay_Colab.ipynb` is a research/prototyping notebook, separate from the workout tracker.

The notebook proposes a small verifier-assisted language-model system intended to combine:

- a Mamba-2/state-space-model backbone
- sparse mixture-of-experts routing
- domain experts for math, code, logic and language
- BPE tokenization
- retrieval through FAISS
- symbolic verification through SymPy
- code verification/execution through Python AST/runtime paths
- logical verification through Z3
- factual verification against retrieved documents
- iterative regeneration/refinement when verification fails

### Proposed energy/objective idea

The notebook describes generation using a conceptual objective of the form:

```text
E(y | x)
 = fluency/model cost
 + λ_math  × mathematical violation cost
 + λ_code  × code violation cost
 + λ_logic × logical violation cost
 + λ_fact  × factual-support violation cost
```

The practical idea is that verifiable domains should be checked by deterministic or symbolic tools rather than trusted solely because the language model produced a confident answer.

### Notebook configuration

The checked-in configuration includes parameters such as:

- vocabulary size: 32,000
- model width: 256
- 8 SSM layers
- Mamba-2 state size: 64
- 4 experts
- top-2 expert routing
- expert hidden dimension: 512
- sequence length: 1,024
- local training target of 20,000 steps
- FAISS top-k retrieval
- verifier weights for math/code/logic/facts
- up to 3 refinement iterations

The notebook targets a Google Colab-style CUDA environment and includes installation commands for packages including `mamba-ssm`, `causal-conv1d`, `tokenizers`, `datasets`, `faiss-cpu`, `sympy`, `z3-solver`, `sentence-transformers`, `accelerate`, and `bitsandbytes`.

### Tokenizer/data path

The notebook proposes training a ByteLevel BPE tokenizer from streamed Wikipedia and GitHub code data and persisting tokenizer/index artifacts to Google Drive.

Because external datasets, package versions and free-Colab hardware limits change over time, the notebook should be treated as an experiment that may require maintenance before it executes end-to-end.

## Important VERA research caveat

The notebook's opening text includes ambitious goals such as beating frontier models on math, code, logic and factual QA. **Those statements are research goals, not verified results in this repository.**

The checked-in notebook cells show `execution_count: null` and no recorded benchmark outputs in the inspected artifact. Therefore this README does not claim that VERA has achieved those benchmark targets.

To make a performance claim credible, the project would need at minimum:

1. reproducible training configuration and exact dependency versions;
2. fixed evaluation datasets/splits;
3. contamination controls;
4. deterministic or statistically reported evaluation runs;
5. baselines evaluated under comparable conditions;
6. complete logs/checkpoints;
7. independent re-runs where possible;
8. latency, memory, compute and cost reporting in addition to accuracy.

## Architectural research notes

The verifier approach can be useful, but its guarantees are limited by the verifier itself.

Examples:

- SymPy can validate many symbolic relations but not every mathematical proof or natural-language assumption.
- Running code successfully does not prove semantic correctness or security.
- Z3 only proves properties that were correctly formalized.
- Retrieval-backed factual checking only covers claims represented in the retrieved corpus and depends on source quality/retrieval recall.
- A verifier-driven retry loop can improve correctness while increasing latency and compute.

Accordingly, “verified” should mean “passed the defined checker under stated assumptions,” not “universally correct.”

## Why the two artifacts are kept separate conceptually

FORM and VERA currently share a Git repository but not an application architecture:

| Area | FORM | VERA |
|---|---|---|
| Purpose | workout tracking | AI architecture research |
| Runtime | browser | Google Colab/Python/CUDA |
| Main file | `index.html` | `VERA_OneDay_Colab.ipynb` |
| Persistence | browser-local | Google Drive/notebook artifacts |
| Build | none | notebook dependency install |
| Backend | none | notebook/runtime services only |

If both projects continue growing, splitting them into separate repositories would improve release history, issue tracking, documentation and dependency management.

## Current status

- FORM is immediately runnable as a static browser app.
- FORM has no cloud account/sync backend in this repository.
- VERA is an experimental notebook/proposal with implementation cells.
- VERA's headline benchmark target is not established by stored result outputs in the checked-in notebook.
- The repository currently has no automated test suite or CI workflow.

## Suggested next steps

### FORM

- extract CSS and JavaScript from the monolithic HTML if development continues;
- add unit tests for timer/state/export calculations;
- version persisted browser data and provide import/export recovery;
- support `prefers-reduced-motion` and accessibility auditing;
- decide whether cross-device sync is actually desired before adding accounts/backend complexity.

### VERA

- pin dependency versions and CUDA/PyTorch compatibility;
- run a minimal smoke-test configuration first;
- separate training, retrieval, verification and evaluation modules from notebook cells;
- add benchmark scripts with raw result exports;
- distinguish verifier pass rate from benchmark accuracy;
- compare against appropriately sized open baselines as well as frontier APIs;
- report compute/time/cost honestly.

## License

No explicit root license file was present before this README. Add separate licensing terms if the workout application and research notebook are intended for public reuse.