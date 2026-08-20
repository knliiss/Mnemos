from __future__ import annotations

import json
import os
import re
import shutil
from pathlib import Path

ROOT = Path("apps/mini-app")
SRC = ROOT / "src"
PRODUCT = SRC / "product-ui"
MOCKUP = Path("apps/mini-app-mockup")


def replace_once(path: Path, old: str, new: str, label: str) -> None:
    text = path.read_text()
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one match, found {count} in {path}")
    path.write_text(text.replace(old, new, 1))


def ensure_import(path: Path, import_line: str) -> None:
    text = path.read_text()
    if import_line in text:
        return
    path.write_text(f"{import_line}\n{text}")


# Copy the accepted mockup profile styling verbatim into a stable production feature stylesheet.
profile_css = PRODUCT / "features/people/public-profile.css"
profile_css.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(MOCKUP / "src/public-profile.css", profile_css)
ensure_import(PRODUCT / "features/people/PeopleArea.tsx", 'import "./public-profile.css"')

# Bypass broken node_modules/.bin shims. npm still installs normal package contents via package-lock.
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

# The notification-service response owns type-specific fields inside `details`.
contracts = SRC / "shared/api/backendContracts.ts"
replace_once(
    contracts,
    '''export type SubscriptionResponse = {\n  id: string\n  type: "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "GLOBAL" | "RAID"\n  createdAt: string\n  droppedFor?: string | null\n  itemName?: string | null\n  boosterType?: BoosterType | null\n  dungeonType?: DungeonType | null\n  eventType?: GlobalEventType | null\n  locations?: number[] | null\n}\n''',
    '''export type SubscriptionResponse =\n  | {\n      id: string\n      type: "ITEM_DROP"\n      createdAt: string\n      details: { droppedFor: string | null; itemName: string | null }\n    }\n  | {\n      id: string\n      type: "BOOSTER"\n      createdAt: string\n      details: { boosterType: BoosterType }\n    }\n  | {\n      id: string\n      type: "DUNGEON"\n      createdAt: string\n      details: { dungeonType: DungeonType }\n    }\n  | {\n      id: string\n      type: "GLOBAL"\n      createdAt: string\n      details: { eventType: GlobalEventType }\n    }\n  | {\n      id: string\n      type: "RAID"\n      createdAt: string\n      details: { locations: number[] }\n    }\n''',
    "subscription backend contract",
)

mappers = PRODUCT / "data/productMappers.ts"
text = mappers.read_text()
start = text.index("export function toProductSubscription")
new_mapper = '''export function toProductSubscription(subscription: SubscriptionResponse): Subscription {\n  if (subscription.type === "ITEM_DROP") {\n    return {\n      id: subscription.id,\n      type: "ITEM_DROP",\n      droppedFor: subscription.details.droppedFor ?? undefined,\n      itemName: subscription.details.itemName ?? undefined,\n    }\n  }\n\n  if (subscription.type === "BOOSTER") {\n    return {\n      id: subscription.id,\n      type: "BOOSTER",\n      boosterType: subscription.details.boosterType,\n    }\n  }\n\n  if (subscription.type === "DUNGEON") {\n    return {\n      id: subscription.id,\n      type: "DUNGEON",\n      dungeonType: subscription.details.dungeonType,\n    }\n  }\n\n  if (subscription.type === "GLOBAL") {\n    return {\n      id: subscription.id,\n      type: "GLOBAL",\n      eventType: subscription.details.eventType,\n    }\n  }\n\n  return {\n    id: subscription.id,\n    type: "RAID",\n    locations: subscription.details.locations,\n  }\n}\n'''
mappers.write_text(text[:start] + new_mapper)

mapper_test = PRODUCT / "data/productMappers.test.ts"
replace_once(
    mapper_test,
    '''  it("maps complete subscriptions and rejects incomplete typed payloads", () => {\n    const booster = {\n      id: "subscription-1",\n      type: "BOOSTER",\n      boosterType: "LUCK",\n    } as Parameters<typeof toProductSubscription>[0]\n    const incomplete = {\n      id: "subscription-2",\n      type: "BOOSTER",\n      boosterType: null,\n    } as Parameters<typeof toProductSubscription>[0]\n\n    expect(toProductSubscription(booster)).toEqual({\n      id: "subscription-1",\n      type: "BOOSTER",\n      boosterType: "LUCK",\n    })\n    expect(toProductSubscription(incomplete)).toBeNull()\n  })\n''',
    '''  it("maps typed subscription details", () => {\n    const booster = {\n      id: "subscription-1",\n      type: "BOOSTER",\n      createdAt: "2026-08-20T00:00:00Z",\n      details: { boosterType: "LUCK" },\n    } as Parameters<typeof toProductSubscription>[0]\n\n    expect(toProductSubscription(booster)).toEqual({\n      id: "subscription-1",\n      type: "BOOSTER",\n      boosterType: "LUCK",\n    })\n  })\n''',
    "subscription mapper test",
)

provider = PRODUCT / "data/ProductDataProvider.tsx"
provider_text = provider.read_text()
provider_text = provider_text.replace(
    '''        response.items\n          .map(toProductSubscription)\n          .filter((subscription): subscription is Subscription => subscription !== null),\n''',
    '''        response.items.map(toProductSubscription),\n''',
)
provider.write_text(provider_text)

# Events already search item name and nickname server-side; use the existing cursor all the way through the UI.
events = PRODUCT / "features/EventsFeed.tsx"
replace_once(
    events,
    'import { useEffect, useState } from "react"',
    'import { useEffect, useMemo, useState } from "react"',
    "events react imports",
)
replace_once(
    events,
    'import { formatOccurredAt } from "../timePresentation"',
    'import { formatOccurredAt } from "../timePresentation"\nimport type { EventFeedQuery } from "@/shared/api/productApi"',
    "events query import",
)
replace_once(
    events,
    '''  const { feed, loadingEvents, refreshEvents, subscriptions } = useProductData()\n''',
    '''  const {\n    feed,\n    eventsHasMore,\n    loadingEvents,\n    refreshEvents,\n    loadMoreEvents,\n    subscriptions,\n  } = useProductData()\n''',
    "events provider destructuring",
)
replace_once(
    events,
    '''  useEffect(() => {\n    const timeout = window.setTimeout(\n      () => {\n        void refreshEvents({\n          types: filter === "ALL" ? undefined : [filter],\n          search: query,\n          sort,\n          limit: 60,\n        })\n      },\n      query.trim() ? 240 : 0,\n    )\n\n    return () => window.clearTimeout(timeout)\n  }, [filter, query, refreshEvents, sort])\n''',
    '''  const feedQuery = useMemo<EventFeedQuery>(\n    () => ({\n      types: filter === "ALL" ? undefined : [filter],\n      search: query,\n      sort,\n      limit: 60,\n    }),\n    [filter, query, sort],\n  )\n\n  useEffect(() => {\n    const timeout = window.setTimeout(\n      () => {\n        void refreshEvents(feedQuery)\n      },\n      query.trim() ? 240 : 0,\n    )\n\n    return () => window.clearTimeout(timeout)\n  }, [feedQuery, query, refreshEvents])\n''',
    "events query lifecycle",
)
replace_once(
    events,
    '''      </section>\n\n      {!loadingEvents && !feed.length ? (\n''',
    '''      </section>\n\n      {eventsHasMore ? (\n        <motion.button\n          type="button"\n          className="events-r2-load-more"\n          whileTap={{ scale: 0.98 }}\n          disabled={loadingEvents}\n          onClick={() => void loadMoreEvents(feedQuery)}\n        >\n          {loadingEvents ? t("events.feed.loading") : t("events.feed.loadMore")}\n        </motion.button>\n      ) : null}\n\n      {!loadingEvents && !feed.length ? (\n''',
    "events load more",
)

# Account home requests its own exact latest-drop slice instead of filtering the global feed snapshot.
account_home = PRODUCT / "features/AccountHome.tsx"
replace_once(
    account_home,
    'import { AnimatePresence, motion } from "framer-motion"\nimport { useEffect, useMemo, useState } from "react"',
    'import { AnimatePresence, motion } from "framer-motion"\nimport { useEffect, useState } from "react"',
    "account home react imports",
)
replace_once(
    account_home,
    '''  revokeMemberRole,\n} from "@/shared/api/productApi"\n''',
    '''  revokeMemberRole,\n  getEventFeed,\n} from "@/shared/api/productApi"\n''',
    "account home event API import",
)
replace_once(
    account_home,
    'import { EventArt } from "../components/EventArt"',
    'import { EventArt } from "../components/EventArt"\nimport { toProductFeedEvent } from "../data/productMappers"',
    "account home event mapper import",
)
replace_once(
    account_home,
    'import type { AccountRole, Member, Route } from "../model"',
    'import type { AccountRole, FeedEvent, Member, Route } from "../model"',
    "account home feed type",
)
old_drops = '''  const drops = useMemo(() => {\n    if (!activeAccount) {\n      return []\n    }\n\n    return product.feed\n      .filter(\n        (event) =>\n          event.kind === "ITEM_DROP" &&\n          event.detail.toLowerCase() === activeAccount.nickname.toLowerCase(),\n      )\n      .slice(0, 3)\n  }, [activeAccount, product.feed])\n'''
new_drops = '''  const [drops, setDrops] = useState<FeedEvent[]>([])\n\n  useEffect(() => {\n    if (!activeAccount) {\n      setDrops([])\n      return\n    }\n\n    let cancelled = false\n\n    void getEventFeed({\n      types: ["ITEM_DROP"],\n      droppedFor: activeAccount.nickname,\n      sort: "NEWEST",\n      limit: 3,\n    })\n      .then((response) => {\n        if (!cancelled) {\n          setDrops(response.items.map(toProductFeedEvent).slice(0, 3))\n        }\n      })\n      .catch(handleError)\n\n    return () => {\n      cancelled = true\n    }\n  }, [activeAccount])\n'''
replace_once(account_home, old_drops, new_drops, "account home dedicated drops")

# Replace hardcoded subscription item fixtures with the real item catalog.
subscriptions = PRODUCT / "features/Subscriptions.tsx"
replace_once(
    subscriptions,
    'import { useMemo, useState } from "react"',
    'import { useEffect, useMemo, useState } from "react"',
    "subscriptions react imports",
)
replace_once(
    subscriptions,
    '''  createRaidSubscription,\n} from "@/shared/api/productApi"\n''',
    '''  createRaidSubscription,\n  searchItemCatalog,\n} from "@/shared/api/productApi"\nimport type { ItemCatalogResponse } from "@/shared/api/backendContracts"\n''',
    "subscriptions catalog import",
)
text = subscriptions.read_text()
start = text.index("type ItemCatalogEntry")
end = text.index("export function Subscriptions")
text = text[:start] + text[end:]
subscriptions.write_text(text)
replace_once(
    subscriptions,
    '''  const [raidInput, setRaidInput] = useState("")\n''',
    '''  const [raidInput, setRaidInput] = useState("")\n  const [itemSuggestions, setItemSuggestions] = useState<ItemCatalogResponse[]>([])\n  const [itemTypesByName, setItemTypesByName] = useState<Record<string, BetItemType>>({})\n''',
    "subscriptions catalog state",
)
text = subscriptions.read_text()
old_suggestions_start = text.index("  const itemSuggestions = useMemo(() => {")
old_suggestions_end = text.index("\n\n  const counts = useMemo", old_suggestions_start)
new_suggestions = '''  useEffect(() => {\n    const query = cleanItemName\n\n    if (query.length < 2) {\n      setItemSuggestions([])\n      return\n    }\n\n    let cancelled = false\n    const timeout = window.setTimeout(() => {\n      void searchItemCatalog(query, 8)\n        .then((items) => {\n          if (cancelled) {\n            return\n          }\n\n          setItemSuggestions(items)\n          setItemTypesByName((current) => ({\n            ...current,\n            ...Object.fromEntries(\n              items.map((item) => [item.name.trim().toLowerCase(), item.type]),\n            ),\n          }))\n        })\n        .catch(handleError)\n    }, 220)\n\n    return () => {\n      cancelled = true\n      window.clearTimeout(timeout)\n    }\n  }, [cleanItemName])\n\n  useEffect(() => {\n    const names = Array.from(\n      new Set(\n        subscriptions\n          .filter((subscription) => subscription.type === "ITEM_DROP" && subscription.itemName)\n          .map((subscription) => subscription.type === "ITEM_DROP" ? subscription.itemName : undefined)\n          .filter((name): name is string => Boolean(name))\n          .filter((name) => !itemTypesByName[name.trim().toLowerCase()]),\n      ),\n    )\n\n    if (!names.length) {\n      return\n    }\n\n    let cancelled = false\n\n    void Promise.all(\n      names.map(async (name) => {\n        const results = await searchItemCatalog(name, 8)\n        const exact = results.find(\n          (item) => item.name.trim().toLowerCase() === name.trim().toLowerCase(),\n        )\n        return exact ? ([name.trim().toLowerCase(), exact.type] as const) : null\n      }),\n    )\n      .then((entries) => {\n        if (!cancelled) {\n          setItemTypesByName((current) => ({\n            ...current,\n            ...Object.fromEntries(entries.filter((entry) => entry !== null)),\n          }))\n        }\n      })\n      .catch(handleError)\n\n    return () => {\n      cancelled = true\n    }\n  }, [itemTypesByName, subscriptions])\n'''
subscriptions.write_text(text[:old_suggestions_start] + new_suggestions + text[old_suggestions_end:])
text = subscriptions.read_text()
text = text.replace("<SubscriptionRuleIcon subscription={subscription} />", "<SubscriptionRuleIcon subscription={subscription} itemTypesByName={itemTypesByName} />")
# Drop the old hardcoded lookup helper and replace the icon signature.
find_start = text.index("function findItem(")
description_start = text.index("function description(", find_start)
text = text[:find_start] + text[description_start:]
old_icon = '''function SubscriptionRuleIcon({ subscription }: { subscription: Subscription }) {\n  if (subscription.type === "ITEM_DROP") {\n    const item = findItem(subscription.itemName)\n\n    if (item) {\n      return <ItemTypeArt itemType={item.type} size={36} />\n    }\n  }\n'''
new_icon = '''function SubscriptionRuleIcon({\n  subscription,\n  itemTypesByName,\n}: {\n  subscription: Subscription\n  itemTypesByName: Record<string, BetItemType>\n}) {\n  if (subscription.type === "ITEM_DROP" && subscription.itemName) {\n    const itemType = itemTypesByName[subscription.itemName.trim().toLowerCase()]\n\n    if (itemType) {\n      return <ItemTypeArt itemType={itemType} size={36} />\n    }\n  }\n'''
if old_icon not in text:
    raise RuntimeError("subscription icon helper did not match")
subscriptions.write_text(text.replace(old_icon, new_icon, 1))

# Route the accepted people implementation and allow genuinely user-scoped routes without an account.
product_app = PRODUCT / "ProductApp.tsx"
replace_once(
    product_app,
    'import { PeopleArea } from "./features/PeopleArea"',
    'import { PeopleArea } from "./features/people/PeopleArea"',
    "people area import",
)
replace_once(
    product_app,
    '''  const notFound = current.params?.notFound === "true"\n  const area = featureArea(current.id)\n''',
    '''  const notFound = current.params?.notFound === "true"\n  const area = featureArea(current.id)\n  const canRenderWithoutActiveAccount =\n    current.id === "user-wallet" ||\n    current.id === "personalization-store" ||\n    current.id === "personalization-store-category" ||\n    current.id === "personalization-detail" ||\n    (current.id === "personalization-owned" && current.params?.target !== "ACCOUNT")\n''',
    "user scoped route capability",
)
replace_once(
    product_app,
    '''  } else if (!activeAccount) {\n    content = <AccountAccessLanding />\n''',
    '''  } else if (!activeAccount && !canRenderWithoutActiveAccount) {\n    content = <AccountAccessLanding />\n''',
    "active account gate",
)

# PeopleArea accepts null only for the user-scoped wallet route.
people_area = PRODUCT / "features/people/PeopleArea.tsx"
people_text = people_area.read_text().replace("  activeAccount: Account\n", "  activeAccount: Account | null\n")
people_text = people_text.replace(
    '''  if (props.routeId === "public-profile") {\n    return (\n''',
    '''  if (props.routeId === "public-profile" && props.activeAccount) {\n    return (\n''',
)
people_area.write_text(people_text)

# Product styles for newly reachable/load-more states.
product_css = PRODUCT / "product.css"
css_text = product_css.read_text()
extra_css = '''\n\n.events-r2-load-more {\n  width: 100%;\n  min-height: 42px;\n  border: 1px solid rgba(225, 232, 208, 0.09);\n  border-radius: 13px;\n  color: #9ba394;\n  background: rgba(255, 255, 255, 0.014);\n  font-size: 9px;\n  font-weight: 680;\n}\n\n.events-r2-load-more:disabled {\n  opacity: 0.55;\n}\n'''
if ".events-r2-load-more" not in css_text:
    product_css.write_text(css_text + extra_css)

# Group product UI files by stable domain. Relative imports are recalculated from the original graph.
def feature_target(path: Path) -> Path | None:
    name = path.name
    stem = path.stem
    if name in {"PeopleArea.tsx", "PeopleFeature.tsx"}:
        return None
    if stem == "UserPrimeWallet":
        return path.parent / "people" / name
    if stem.startswith("Account") or stem.startswith("Role") or stem.startswith("Member"):
        return path.parent / "account" / name
    if stem.startswith("BetAdmin") or stem.startswith("Moderation") or stem.startswith("Administration") or stem.startswith("Staff") or stem.startswith("Badge"):
        return path.parent / "staff" / name
    if stem.startswith("Bets"):
        return path.parent / "bets" / name
    if stem.startswith("Event") or stem == "Subscriptions":
        return path.parent / "events" / name
    if stem.startswith("Personalization"):
        return path.parent / "personalization" / name
    if stem.startswith("Creator"):
        return path.parent / "creator" / name
    if stem in {"FeatureLoading", "NotFoundFeature"}:
        return path.parent / "system" / name
    return path


def component_target(path: Path) -> Path:
    stem = path.stem.split(".")[0]
    if stem == "CristalixHead":
        bucket = "profile"
    elif stem.startswith("Error"):
        bucket = "errors"
    elif stem == "EventArt":
        bucket = "events"
    elif stem == "PersonalizationPreview":
        bucket = "personalization"
    elif stem == "RoleSigil":
        bucket = "roles"
    elif stem == "RootNavIllustration":
        bucket = "navigation"
    else:
        raise RuntimeError(f"Unclassified component: {path}")
    return path.parent / bucket / path.name


def data_target(path: Path) -> Path:
    if path.name in {"ProductDataProvider.tsx", "ProductLanguageSync.tsx"}:
        bucket = "state"
    else:
        bucket = "mappers"
    return path.parent.parent / bucket / path.name


module_files = {
    path
    for path in SRC.rglob("*")
    if path.is_file() and path.suffix in {".ts", ".tsx"}
}
mapping: dict[Path, Path] = {path: path for path in module_files}

features_root = PRODUCT / "features"
for path in list(features_root.glob("*.tsx")):
    target = feature_target(path)
    if target is None:
        continue
    mapping[path] = target

components_root = PRODUCT / "components"
for path in list(components_root.glob("*.ts*")):
    mapping[path] = component_target(path)

data_root = PRODUCT / "data"
for path in list(data_root.glob("*.ts*")):
    mapping[path] = data_target(path)

# App uses aliases, so update the two state imports explicitly before rewriting relative modules.
app_file = SRC / "app/App.tsx"
app_text = app_file.read_text()
app_text = app_text.replace(
    "@/product-ui/data/ProductDataProvider",
    "@/product-ui/state/ProductDataProvider",
)
app_text = app_text.replace(
    "@/product-ui/data/ProductLanguageSync",
    "@/product-ui/state/ProductLanguageSync",
)
app_file.write_text(app_text)

contents = {path: path.read_text() for path in module_files}


def resolve_module(source: Path, specifier: str) -> Path | None:
    base = (source.parent / specifier).resolve()
    candidates = [
        Path(str(base)),
        Path(str(base) + ".ts"),
        Path(str(base) + ".tsx"),
        base / "index.ts",
        base / "index.tsx",
    ]
    resolved_files = {path.resolve(): path for path in module_files}
    for candidate in candidates:
        if candidate in resolved_files:
            return resolved_files[candidate]
    return None


relative_pattern = re.compile(r'(["\'])(\.{1,2}/[^"\']+)\1')
rewritten: dict[Path, str] = {}
for old_source, content in contents.items():
    new_source = mapping.get(old_source, old_source)

    def rewrite_match(match: re.Match[str]) -> str:
        quote = match.group(1)
        specifier = match.group(2)
        old_target = resolve_module(old_source, specifier)
        if old_target is None:
            return match.group(0)
        new_target = mapping.get(old_target, old_target)
        relative = os.path.relpath(new_target, new_source.parent).replace(os.sep, "/")
        if new_target.suffix in {".ts", ".tsx"}:
            relative = relative[: -len(new_target.suffix)]
        if not relative.startswith("."):
            relative = "./" + relative
        return f"{quote}{relative}{quote}"

    rewritten[new_source] = relative_pattern.sub(rewrite_match, content)

# Remove superseded mixed people files before writing the new graph.
for obsolete in [features_root / "PeopleArea.tsx", features_root / "PeopleFeature.tsx"]:
    if obsolete.exists():
        obsolete.unlink()

# Remove old paths that are being moved.
for old_path, new_path in mapping.items():
    if old_path != new_path and old_path.exists():
        old_path.unlink()

for new_path, content in rewritten.items():
    new_path.parent.mkdir(parents=True, exist_ok=True)
    new_path.write_text(content)

# Fail if the refactor left unrelated feature/component files in the old flat roots.
feature_leftovers = [path.name for path in features_root.glob("*.tsx")]
component_leftovers = [path.name for path in components_root.glob("*.ts*")]
data_leftovers = [path.name for path in data_root.glob("*.ts*")]
if feature_leftovers or component_leftovers or data_leftovers:
    raise RuntimeError(
        f"Flat package leftovers: features={feature_leftovers}, components={component_leftovers}, data={data_leftovers}"
    )

# Re-apply the new people Area after old module-graph rewriting; it did not exist in the old flat package.
ensure_import(PRODUCT / "features/people/PeopleArea.tsx", 'import "./public-profile.css"')

print("Final mini-app migration applied")
