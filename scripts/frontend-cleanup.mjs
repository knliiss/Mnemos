import fs from "node:fs"
import { createRequire } from "node:module"
import path from "node:path"

const repositoryRoot = process.cwd()
const miniAppRoot = path.join(repositoryRoot, "apps/mini-app")
const sourceRoot = path.join(miniAppRoot, "src")
const productCssPath = path.join(sourceRoot, "product-ui/product.css")
const globalCssPath = path.join(sourceRoot, "shared/styles/global.css")
const require = createRequire(path.join(miniAppRoot, "package.json"))
const { PurgeCSS } = require("purgecss")

const exactRenames = new Map([
  ["redesign-stage", "preview-stage"],
  ["final-stage", "preview-stage"],
  ["redesign-shell", "app-shell"],
  ["final-shell", "app-shell"],
  ["final-navigation", "root-navigation"],
  ["global-user-button--final", "global-user-button"],
  ["user-hub--final", "user-hub"],
  ["workspace-grid--final", "workspace-grid"],
  ["administration-home-screen--final", "administration-home-screen"],
  ["moderation-form--final", "moderation-form"],
  ["moderation-actions-final", "moderation-actions"],
  ["sanction-list--final", "sanction-list"],
  ["subscription-form-redesign", "subscription-form"],
  ["subscriptions-redesign-screen", "subscriptions-screen"],
  ["invite-create-final", "invite-create-panel"],
  ["invite-final-screen", "invites-screen"],
  ["invites-screen-redesign", "invites-screen"],
  ["account-wallet-v2", "account-wallet-screen"],
  ["account-settings-v2", "account-settings-screen"],
  ["bet-admin-v2", "bet-admin-screen"],
  ["prime-wallet-v2", "prime-wallet-screen"],
  ["role-editor-v2", "role-editor-screen"],
  ["roles-v2", "roles-screen"],
  ["account-member-v4", "account-home-member-detail"],
  ["account-v5-reward-particles", "account-home-reward-particles"],
  ["account-personalization-category-v5", "account-personalization-category"],
  ["account-personalization-v5", "account-personalization-overview"],
  ["owned-v5-list", "personalization-owned-list"],
  ["owned-v5-screen", "personalization-owned-screen"],
  ["personalization-detail-v5", "personalization-detail-screen"],
  ["store-category-v5", "personalization-store-category"],
  ["store-v5-categories", "personalization-store-categories"],
  ["store-v5-header", "personalization-store-header"],
  ["store-v5-hero", "personalization-store-hero"],
  ["store-v5-screen", "personalization-store-screen"],
])

function walk(directory) {
  if (!fs.existsSync(directory)) {
    return []
  }

  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name)

    return entry.isDirectory() ? walk(entryPath) : [entryPath]
  })
}

function sourceFiles() {
  return walk(sourceRoot).filter((filePath) => /\.(?:ts|tsx|css)$/.test(filePath))
}

function renameToken(token) {
  const exact = exactRenames.get(token)

  if (exact) {
    return exact
  }

  if (token.startsWith("final-user-card")) {
    return token.replace("final-user-card", "user-hub-card")
  }

  if (token.startsWith("final-user-prime")) {
    return token.replace("final-user-prime", "user-hub-prime")
  }

  if (token.startsWith("final-account-picker")) {
    return token.replace("final-account-picker", "user-hub-account-picker")
  }

  if (token.startsWith("final-account-switch")) {
    return token.replace("final-account-switch", "user-hub-account-switch")
  }

  if (token.startsWith("final-workspace-grid")) {
    return token.replace("final-workspace-grid", "user-hub-workspace-grid")
  }

  if (token.startsWith("final-workspace-label")) {
    return token.replace("final-workspace-label", "user-hub-workspace-label")
  }

  if (token.startsWith("invite-final-")) {
    return token.replace("invite-final-", "invite-")
  }

  if (token === "sanction-final") {
    return "sanction-card"
  }

  if (token.startsWith("sanction-final--")) {
    return token.replace("sanction-final--", "sanction-card--")
  }

  if (token.startsWith("moderation-polished-results")) {
    return token.replace("moderation-polished-results", "staff-search-results")
  }

  if (token === "moderation-polished-search") {
    return "staff-search"
  }

  if (token.startsWith("moderation-polished-")) {
    return token.replace("moderation-polished-", "moderation-")
  }

  if (token.startsWith("account-v4-")) {
    return token.replace("account-v4-", "account-home-")
  }

  if (token.startsWith("account-v4")) {
    return token.replace("account-v4", "account-home")
  }

  if (token.startsWith("store-v5-category")) {
    return token.replace("store-v5-category", "personalization-store-category-card")
  }

  if (token.startsWith("store-v5-")) {
    return token.replace("store-v5-", "personalization-store-")
  }

  if (token.startsWith("account-settings-v2-")) {
    return token.replace("account-settings-v2-", "account-settings-")
  }

  if (token.startsWith("bet-admin-v2-")) {
    return token.replace("bet-admin-v2-", "bet-admin-")
  }

  if (token.startsWith("role-editor-v2-")) {
    return token.replace("role-editor-v2-", "role-editor-")
  }

  if (token.startsWith("roles-v2-")) {
    return token.replace("roles-v2-", "roles-")
  }

  if (token.startsWith("role-v2-")) {
    return token.replace("role-v2-", "role-")
  }

  if (token.includes("-v2-")) {
    return token.replaceAll("-v2-", "-")
  }

  if (token.endsWith("-v2")) {
    return token.slice(0, -3)
  }

  return token
}

function renameHistoricalClasses(filePath) {
  const original = fs.readFileSync(filePath, "utf8")
  let content = original.replace(/[A-Za-z_][A-Za-z0-9_-]*/g, renameToken)

  content = content
    .replaceAll('className="preview-stage preview-stage preview-stage"', 'className="preview-stage"')
    .replaceAll('className="app-shell app-shell app-shell"', 'className="app-shell"')
    .replaceAll(
      'className="root-navigation root-navigation--five root-navigation root-navigation--illustrated"',
      'className="root-navigation root-navigation--five root-navigation--illustrated"',
    )
    .replaceAll('className="global-user-button global-user-button ', 'className="global-user-button ')
    .replaceAll('className="user-hub user-hub"', 'className="user-hub"')
    .replaceAll('className="workspace-grid workspace-grid"', 'className="workspace-grid"')
    .replaceAll('className="invites-screen invites-screen"', 'className="invites-screen"')

  if (content !== original) {
    fs.writeFileSync(filePath, content)
  }
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")
}

function collectDynamicClassPatterns(files) {
  const patterns = new Map()

  for (const filePath of files.filter((candidate) => /\.(?:ts|tsx)$/.test(candidate))) {
    const content = fs.readFileSync(filePath, "utf8")

    for (const templateMatch of content.matchAll(/`([\s\S]*?)`/g)) {
      const template = templateMatch[1]

      for (const classMatch of template.matchAll(/[A-Za-z_][A-Za-z0-9_-]*\$\{[^}]+\}[A-Za-z0-9_-]*/g)) {
        const dynamicClass = classMatch[0]
        const regexSource = dynamicClass
          .split(/\$\{[^}]+\}/g)
          .map(escapeRegExp)
          .join(".+")

        patterns.set(regexSource, new RegExp(`^${regexSource}$`))
      }
    }
  }

  return [...patterns.values()]
}

async function purgeStylesheet(cssPath, contentPaths, dynamicPatterns) {
  const beforeBytes = fs.statSync(cssPath).size
  const [result] = await new PurgeCSS().purge({
    content: contentPaths,
    css: [cssPath],
    defaultExtractor: (content) => content.match(/[A-Za-z0-9_:/-]+/g) ?? [],
    safelist: {
      greedy: dynamicPatterns,
    },
    fontFace: true,
    keyframes: true,
    rejected: true,
  })

  fs.writeFileSync(cssPath, result.css.endsWith("\n") ? result.css : `${result.css}\n`)

  const afterBytes = fs.statSync(cssPath).size
  const removedSelectors = result.rejected?.length ?? 0
  const reduction = beforeBytes === 0 ? 0 : Math.round((1 - afterBytes / beforeBytes) * 100)

  console.log(
    `${path.relative(repositoryRoot, cssPath)}: ${beforeBytes} -> ${afterBytes} bytes (${reduction}% smaller), ${removedSelectors} selectors removed`,
  )
}

function historicalTokens(files) {
  const historicalPattern = /(?:final|polished|redesign|(?:^|-)v[2-9](?:-|$))/i
  const usages = new Map()

  for (const filePath of files) {
    const content = fs.readFileSync(filePath, "utf8")
    const tokens = content.match(/[A-Za-z_][A-Za-z0-9_-]*/g) ?? []

    for (const token of tokens) {
      if (!token.includes("-") || !historicalPattern.test(token)) {
        continue
      }

      const relativePath = path.relative(repositoryRoot, filePath)
      const paths = usages.get(token) ?? new Set()

      paths.add(relativePath)
      usages.set(token, paths)
    }
  }

  return usages
}

const filesBeforeRename = sourceFiles()

for (const filePath of filesBeforeRename) {
  renameHistoricalClasses(filePath)
}

const files = sourceFiles()
const contentPaths = [
  path.join(miniAppRoot, "index.html"),
  ...files.filter((filePath) => /\.(?:ts|tsx)$/.test(filePath)),
]
const dynamicPatterns = collectDynamicClassPatterns(files)

console.log(`Safelisting ${dynamicPatterns.length} concrete dynamic class patterns`)

await purgeStylesheet(productCssPath, contentPaths, dynamicPatterns)
await purgeStylesheet(globalCssPath, contentPaths, dynamicPatterns)

const remaining = historicalTokens(sourceFiles())

if (remaining.size > 0) {
  console.error(`Historical class tokens remain: ${remaining.size}`)

  for (const [token, paths] of [...remaining.entries()].sort(([left], [right]) => left.localeCompare(right))) {
    console.error(`${token}\t${[...paths].sort().join(", ")}`)
  }

  process.exit(1)
}

console.log("Historical class-name audit passed")
