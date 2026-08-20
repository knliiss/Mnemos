from __future__ import annotations

import hashlib
import json
import os
import re
from pathlib import Path

ROOT = Path("apps/mini-app")
SRC = ROOT / "src"
PRODUCT = SRC / "product-ui"

TEMPORARY_FILES = (
    Path(".github/workflows/frontend-cleanup.yml"),
    Path(".github/workflows/finalize-mini-app.yml"),
    Path("scripts/frontend-cleanup.mjs"),
    Path("scripts/finalize-mini-app.py"),
)

FINAL_VERIFY_WORKFLOW = '''name: Mini App Verify

on:
  push:
    paths:
      - "apps/mini-app/**"
      - ".github/workflows/mini-app.yml"
  pull_request:
    paths:
      - "apps/mini-app/**"
      - ".github/workflows/mini-app.yml"

concurrency:
  group: mini-app-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read
  statuses: write

jobs:
  verify:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: apps/mini-app

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Node
        uses: actions/setup-node@v4
        with:
          node-version: 22
          cache: npm
          cache-dependency-path: apps/mini-app/package-lock.json

      - name: Install dependencies
        run: npm ci

      - name: Audit dependencies
        run: npm audit --audit-level=high

      - name: Verify frontend
        run: npm run verify

      - name: Check bundle budgets
        shell: bash
        run: |
          set -euo pipefail

          oversized_js="$(find dist/assets -type f -name '*.js' -size +500k -print)"
          if [ -n "$oversized_js" ]; then
            echo "JavaScript chunks over 500 KiB:"
            echo "$oversized_js"
            exit 1
          fi

          oversized_css="$(find dist/assets -type f -name '*.css' -size +450k -print)"
          if [ -n "$oversized_css" ]; then
            echo "CSS chunks over 450 KiB:"
            echo "$oversized_css"
            exit 1
          fi

      - name: Publish verification status
        if: always() && github.event_name == 'push'
        working-directory: .
        shell: bash
        env:
          GH_TOKEN: ${{ github.token }}
          JOB_STATUS: ${{ job.status }}
          RUN_URL: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}
        run: |
          set -euo pipefail

          if [ "$JOB_STATUS" = "success" ]; then
            state="success"
          else
            state="failure"
          fi

          payload="$(jq -n \
            --arg state "$state" \
            --arg target_url "$RUN_URL" \
            --arg description "Production Mini App verify: $JOB_STATUS" \
            --arg context "mini-app/verify" \
            '{state:$state,target_url:$target_url,description:$description,context:$context}')"

          curl --fail-with-body \
            --request POST \
            --header "Accept: application/vnd.github+json" \
            --header "Authorization: Bearer $GH_TOKEN" \
            --header "X-GitHub-Api-Version: 2022-11-28" \
            --data "$payload" \
            "${{ github.api_url }}/repos/${{ github.repository }}/statuses/${{ github.sha }}"
'''

README = '''# Mnemos Mini App

Production Telegram Mini App frontend for Mnemos.

## Runtime

- Node.js 22.12+ (Node 25 is supported by the project scripts)
- npm
- Vite + React + TypeScript

Install and run:

```bash
npm ci
npm run dev
```

The npm scripts execute package entrypoints directly instead of relying on `node_modules/.bin` shims. This avoids the macOS/npm failure where a copied Vite shim resolves `../dist/node/cli.js` from `node_modules/.bin` and throws `ERR_MODULE_NOT_FOUND`.

If an existing checkout has stale dependencies, reinstall them once:

```bash
rm -rf node_modules
npm ci
```

## Verification

```bash
npm run verify
```

`verify` runs ESLint, Prettier check, Vitest and the production Vite build. CI additionally runs `npm audit --audit-level=high` and enforces bundle budgets.

## Data policy

Production screens use backend APIs only. Mock fixtures and mock fallback state are not allowed in `apps/mini-app` production paths.

See `FRONTEND_ARCHITECTURE.md` for package boundaries.
'''

ARCHITECTURE = '''# Mini App frontend architecture

`apps/mini-app` is the production frontend. The old mockup is a design/reference application and must not be imported from production code.

## Package boundaries

```text
src/
  app/                    application composition and providers
  features/auth/          Telegram/Keycloak authentication gate
  pages/loading/          app bootstrap loading screen
  product-ui/
    shell/                routing stack, root navigation and user hub
    features/
      account/            account home, members, roles, invites, settings, account wallet
      bets/               user betting flows
      creator/            creator studio
      events/             event feed, details and subscriptions
      people/             account search, public profile, comments, user wallet
      personalization/    catalog, owned items and details
      staff/              moderation/administration/review tools
      system/             loading/not-found product states
    components/
      errors/              product error presentation
      events/              event artwork
      navigation/          navigation artwork
      personalization/     personalization previews
      profile/             Cristalix avatar/head rendering
      roles/               role visuals
    data/                  API-to-product mappers only
    state/                 product state providers/synchronization
    navigation/            route registry
    shared/                truly shared UI primitives/icons
  shared/
    api/                   HTTP contracts and transport functions
    errors/                global error handling
    i18n/                  localization resources
    notifications/         global notification host
    realtime/              SSE client/event bus
    styles/                app-global styles
```

## Rules

- One responsibility has one canonical module path. Compatibility re-export files are not kept after migrations.
- Domain screens live in their domain package; unrelated screens are not mixed in one flat `features` directory.
- Backend transport stays under `shared/api`. UI components do not create ad-hoc fetch clients.
- Product-level state belongs in `product-ui/state`; response-to-view mapping belongs in `product-ui/data`.
- Production code must not import mock data, fixture stores, mockup modules, or local fake API fallbacks.
- Public profile responses may expose account identity, but not the Mnemos user identity behind an account. User-specific actions are represented as server-computed capabilities such as `canDelete`.
- CSS is organized by stable product/domain responsibility. Revision names such as `final`, `round`, `v4`, `polished`, or `overhaul` are not valid production names.
- Prettier and ESLint are mandatory. Braced control flow is enforced by ESLint.
- `npm run verify` must pass before frontend changes are considered complete.
'''


def configure_package() -> None:
    package_path = ROOT / "package.json"
    package = json.loads(package_path.read_text())

    package["scripts"].update(
        {
            "dev": "node ./node_modules/vite/bin/vite.js",
            "build": "node ./node_modules/typescript/bin/tsc -b && node ./node_modules/vite/bin/vite.js build",
            "lint": "node ./node_modules/eslint/bin/eslint.js .",
            "test": "node ./node_modules/vitest/vitest.mjs run",
            "format": "node ./node_modules/prettier/bin/prettier.cjs --write src vite.config.ts eslint.config.js",
            "format:check": "node ./node_modules/prettier/bin/prettier.cjs --check src vite.config.ts eslint.config.js",
            "verify": "npm run lint && npm run format:check && npm test && npm run build",
        }
    )
    package["engines"] = {"node": ">=22.12"}

    package_path.write_text(json.dumps(package, ensure_ascii=False, indent=2) + "\n")
    (ROOT / ".npmrc").write_text("bin-links=true\nfund=false\n")


def align_subscription_contract() -> None:
    path = SRC / "shared/api/backendContracts.ts"
    text = path.read_text()
    old = '''export type SubscriptionResponse = {\n  id: string\n  type: "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "GLOBAL" | "RAID"\n  createdAt: string\n  droppedFor?: string | null\n  itemName?: string | null\n  boosterType?: BoosterType | null\n  dungeonType?: DungeonType | null\n  eventType?: GlobalEventType | null\n  locations?: number[] | null\n}\n'''
    new = '''export type SubscriptionResponse =\n  | {\n      id: string\n      type: "ITEM_DROP"\n      createdAt: string\n      details: { droppedFor: string | null; itemName: string | null }\n    }\n  | {\n      id: string\n      type: "BOOSTER"\n      createdAt: string\n      details: { boosterType: BoosterType }\n    }\n  | {\n      id: string\n      type: "DUNGEON"\n      createdAt: string\n      details: { dungeonType: DungeonType }\n    }\n  | {\n      id: string\n      type: "GLOBAL"\n      createdAt: string\n      details: { eventType: GlobalEventType }\n    }\n  | {\n      id: string\n      type: "RAID"\n      createdAt: string\n      details: { locations: number[] }\n    }\n'''

    if old in text:
        path.write_text(text.replace(old, new, 1))
        return

    if "details: { droppedFor: string | null; itemName: string | null }" not in text:
        raise RuntimeError("Unknown SubscriptionResponse contract shape")


def make_account_drops_query_specific() -> None:
    path = PRODUCT / "features/AccountHome.tsx"
    if not path.exists():
        path = PRODUCT / "features/account/AccountHome.tsx"
    if not path.exists():
        return

    text = path.read_text()
    if "getEventFeed" in text and "setDrops" in text:
        return

    text = text.replace(
        'import { useEffect, useMemo, useState } from "react"',
        'import { useEffect, useState } from "react"',
    )
    text = text.replace(
        '''  kickAccountMember,\n  revokeMemberRole,\n} from "@/shared/api/productApi"''',
        '''  getEventFeed,\n  kickAccountMember,\n  revokeMemberRole,\n} from "@/shared/api/productApi"''',
    )
    text = text.replace(
        'import { EventArt } from "../components/EventArt"',
        'import { EventArt } from "../components/EventArt"\nimport { toProductFeedEvent } from "../data/productMappers"',
    )
    text = text.replace(
        'import type { AccountRole, Member, Route } from "../model"',
        'import type { AccountRole, FeedEvent, Member, Route } from "../model"',
    )

    old = '''  const drops = useMemo(() => {\n    if (!activeAccount) {\n      return []\n    }\n\n    return product.feed\n      .filter(\n        (event) =>\n          event.kind === "ITEM_DROP" &&\n          event.detail.toLowerCase() === activeAccount.nickname.toLowerCase(),\n      )\n      .slice(0, 3)\n  }, [activeAccount, product.feed])\n'''
    new = '''  const [drops, setDrops] = useState<FeedEvent[]>([])\n\n  useEffect(() => {\n    if (!activeAccount) {\n      setDrops([])\n      return\n    }\n\n    let cancelled = false\n\n    void getEventFeed({\n      types: ["ITEM_DROP"],\n      droppedFor: activeAccount.nickname,\n      sort: "NEWEST",\n      limit: 3,\n    })\n      .then((response) => {\n        if (!cancelled) {\n          setDrops(response.items.map(toProductFeedEvent).slice(0, 3))\n        }\n      })\n      .catch(handleError)\n\n    return () => {\n      cancelled = true\n    }\n  }, [activeAccount])\n'''

    if old not in text:
        raise RuntimeError("AccountHome drop source no longer matches expected production code")

    path.write_text(text.replace(old, new, 1))


def feature_target(path: Path) -> Path:
    features = PRODUCT / "features"
    name = path.name
    stem = path.stem

    if path.parent != features:
        if path.as_posix().endswith("features/events/Bets.tsx"):
            return features / "bets/Bets.tsx"
        if path.as_posix().endswith("features/account/AccountHomePage.tsx"):
            return features / "account/AccountHome.tsx"
        return path

    if stem == "UserPrimeWallet":
        return features / "people" / name
    if stem.startswith(("Account", "Role", "Member")):
        return features / "account" / name
    if stem == "Bets":
        return features / "bets" / name
    if stem.startswith("Creator"):
        return features / "creator" / name
    if stem.startswith("Event") or stem == "Subscriptions":
        return features / "events" / name
    if stem.startswith("Personalization") and stem != "PersonalizationReview":
        return features / "personalization" / name
    if stem.startswith(("Administration", "Badge", "BetAdmin", "Moderation", "Staff")) or stem == "PersonalizationReview":
        return features / "staff" / name
    if stem in {"FeatureLoading", "NotFoundFeature"}:
        return features / "system" / name

    return path


def component_target(path: Path) -> Path:
    components = PRODUCT / "components"

    if path == PRODUCT / "shared/RoleSigil.tsx" or path == components / "RoleSigil.tsx":
        return components / "roles/RoleSigil.tsx"

    if path.parent != components:
        return path

    stem = path.stem.split(".")[0]
    if stem == "CristalixHead":
        bucket = "profile"
    elif stem.startswith("Error"):
        bucket = "errors"
    elif stem == "EventArt":
        bucket = "events"
    elif stem == "PersonalizationPreview":
        bucket = "personalization"
    elif stem == "RootNavIllustration":
        bucket = "navigation"
    else:
        return path

    return components / bucket / path.name


def data_target(path: Path) -> Path:
    data = PRODUCT / "data"

    if path == data / "ProductDataProvider.tsx" or path == PRODUCT / "state/ProductDataProvider.tsx":
        return PRODUCT / "state/ProductDataProvider.tsx"
    if path == data / "ProductLanguageSync.tsx" or path == PRODUCT / "state/ProductLanguageSync.tsx":
        return PRODUCT / "state/ProductLanguageSync.tsx"
    if path.parent == data:
        return PRODUCT / "data" / path.name

    return path


def canonical_target(path: Path) -> Path:
    if path == PRODUCT / "ProductApp.tsx" or path == PRODUCT / "shell/ProductShell.tsx":
        return PRODUCT / "shell/ProductShell.tsx"

    target = feature_target(path)
    target = component_target(target)
    target = data_target(target)
    return target


def resolve_module(source: Path, specifier: str, modules: set[Path]) -> Path | None:
    if specifier.startswith("@/"):
        base = SRC / specifier[2:]
    elif specifier.startswith("."):
        base = source.parent / specifier
    else:
        return None

    candidates = (
        base,
        Path(f"{base}.ts"),
        Path(f"{base}.tsx"),
        base / "index.ts",
        base / "index.tsx",
    )

    resolved = {candidate.resolve(): candidate for candidate in modules}
    for candidate in candidates:
        match = resolved.get(candidate.resolve())
        if match is not None:
            return match

    return None


def rewrite_specifiers(
    source: Path,
    target: Path,
    content: str,
    mapping: dict[Path, Path],
    modules: set[Path],
) -> str:
    pattern = re.compile(r'(["\'])(@/[^"\']+|\.{1,2}/[^"\']+)\1')

    def replace(match: re.Match[str]) -> str:
        quote = match.group(1)
        specifier = match.group(2)
        resolved = resolve_module(source, specifier, modules)
        if resolved is None:
            return match.group(0)

        final_target = mapping[resolved]
        if specifier.startswith("@/"):
            alias = "@/" + final_target.relative_to(SRC).as_posix()
            if final_target.suffix in {".ts", ".tsx"}:
                alias = alias[: -len(final_target.suffix)]
            return f"{quote}{alias}{quote}"

        relative = os.path.relpath(final_target, target.parent).replace(os.sep, "/")
        if final_target.suffix in {".ts", ".tsx"}:
            relative = relative[: -len(final_target.suffix)]
        if not relative.startswith("."):
            relative = "./" + relative
        return f"{quote}{relative}{quote}"

    return pattern.sub(replace, content)


def normalize_product_modules() -> None:
    modules = {
        path
        for path in SRC.rglob("*")
        if path.is_file() and path.suffix in {".ts", ".tsx"}
    }
    mapping = {path: canonical_target(path) for path in modules}

    candidates_by_target: dict[Path, list[Path]] = {}
    for source, target in mapping.items():
        candidates_by_target.setdefault(target, []).append(source)

    preferred: dict[Path, Path] = {}
    for target, candidates in candidates_by_target.items():
        preferred[target] = max(
            candidates,
            key=lambda candidate: (candidate.stat().st_size, candidate == target),
        )

    rewritten: dict[Path, str] = {}
    for target, source in preferred.items():
        content = source.read_text()
        rewritten[target] = rewrite_specifiers(
            source=source,
            target=target,
            content=content,
            mapping=mapping,
            modules=modules,
        )

    for source in modules:
        target = mapping[source]
        if source != target or preferred[target] != source:
            source.unlink(missing_ok=True)

    for target, content in rewritten.items():
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content)

    product_app = PRODUCT / "ProductApp.tsx"
    product_app.unlink(missing_ok=True)

    old_data_provider = PRODUCT / "data/ProductDataProvider.tsx"
    old_data_provider.unlink(missing_ok=True)


def write_docs() -> None:
    (ROOT / "README.md").write_text(README)
    (ROOT / "FRONTEND_ARCHITECTURE.md").write_text(ARCHITECTURE)


def remove_empty_directories() -> None:
    for directory in sorted(
        (path for path in PRODUCT.rglob("*") if path.is_dir()),
        key=lambda path: len(path.parts),
        reverse=True,
    ):
        try:
            directory.rmdir()
        except OSError:
            pass


def audit_production_tree() -> None:
    flat_features = list((PRODUCT / "features").glob("*.tsx"))
    flat_components = list((PRODUCT / "components").glob("*.ts*"))
    legacy_data = [
        path
        for path in (PRODUCT / "data").glob("*.ts*")
        if path.name in {"ProductDataProvider.tsx", "ProductLanguageSync.tsx"}
    ]

    if flat_features or flat_components or legacy_data:
        raise RuntimeError(
            "Flat package leftovers: "
            f"features={[path.name for path in flat_features]}, "
            f"components={[path.name for path in flat_components]}, "
            f"data={[path.name for path in legacy_data]}"
        )

    forbidden = re.compile(r"(?:Final|Polished|Overhaul|Rethink|Round(?:One|Two|Three|Four|Five|Six|Seven)|-v[2-9]|mockData|MockStore)")
    bad_paths = [
        path.relative_to(ROOT).as_posix()
        for path in PRODUCT.rglob("*")
        if path.is_file() and forbidden.search(path.name)
    ]
    if bad_paths:
        raise RuntimeError(f"Historical/mock production paths remain: {bad_paths}")

    source_files = [
        path
        for path in PRODUCT.rglob("*")
        if path.is_file() and path.suffix in {".ts", ".tsx"}
    ]
    by_digest: dict[str, list[Path]] = {}
    for path in source_files:
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        by_digest.setdefault(digest, []).append(path)

    duplicates = [
        [path.relative_to(PRODUCT).as_posix() for path in paths]
        for paths in by_digest.values()
        if len(paths) > 1
    ]
    if duplicates:
        raise RuntimeError(f"Exact duplicate production modules remain: {duplicates}")


def finalize_repository_metadata() -> None:
    Path(".github/workflows/mini-app.yml").write_text(FINAL_VERIFY_WORKFLOW)

    for path in TEMPORARY_FILES:
        path.unlink(missing_ok=True)


configure_package()
align_subscription_contract()
make_account_drops_query_specific()
normalize_product_modules()
write_docs()
remove_empty_directories()
audit_production_tree()
finalize_repository_metadata()

print("Production mini-app finalization completed")
