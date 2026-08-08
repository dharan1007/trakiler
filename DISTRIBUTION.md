# FORM Distribution

FORM currently has three public distribution surfaces:

1. **GitHub Pages** — fastest way to evaluate the current browser build.
2. **GitHub Releases** — versioned public-web archives, checksums and release notes.
3. **GitHub Container Registry (GHCR)** — reproducible containerized public-web build.

## Live web

https://dharan1007.github.io/trakiler/

## Releases

Latest release page:

https://github.com/dharan1007/trakiler/releases/latest

Versioned releases provide:

- curated browser-runtime ZIP;
- curated browser-runtime tar.gz;
- SHA-256 checksum file;
- GitHub-generated source archives;
- versioned release notes.

The release artifact intentionally excludes the Supabase schema directory and research notebook. It contains only the public browser experience and selected public documentation.

## GitHub Container Registry

Image name:

```text
ghcr.io/dharan1007/form-training-system
```

Published tags are designed to include:

```text
latest
0.9.0
sha-<git-commit>
```

When the package visibility is set to **Public** in GitHub Package settings, anyone can pull it without authenticating to GHCR:

```bash
docker pull ghcr.io/dharan1007/form-training-system:latest
docker run --rm -p 8080:80 ghcr.io/dharan1007/form-training-system:latest
```

Then open:

```text
http://localhost:8080
```

### Package-visibility requirement

For a package published under a personal GitHub account, verify the package's visibility in GitHub after the first publish. If it is still Private, change it to Public only when you are intentionally ready for unrestricted public access.

GitHub UI path:

```text
GitHub profile
→ Packages
→ form-training-system
→ Package settings
→ Danger Zone / Change visibility
→ Public
```

Treat this as an irreversible publication decision for the package version/history according to GitHub's package-visibility rules. Never publish credentials or private source into the image.

## Reproducibility and provenance

The container workflow publishes OCI source/revision/version labels, build provenance and an SBOM through GitHub Actions. Release archives include SHA-256 digests for integrity checking.

## What is not a distribution surface

The following should not be published merely to increase repository impressions:

- fake npm/PyPI packages with no reusable library/API;
- empty releases;
- duplicated repositories containing the same code;
- irrelevant GitHub topics;
- automated issue/comment spam;
- private backend or confidential research material.

Distribution should make FORM easier to evaluate, reproduce and discuss—not manufacture misleading activity metrics.
