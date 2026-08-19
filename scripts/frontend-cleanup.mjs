import fs from "node:fs"
import path from "node:path"

const repositoryRoot = process.cwd()
const miniAppRoot = path.join(repositoryRoot, "apps/mini-app")
const sourceRoot = path.join(miniAppRoot, "src")

const sourceExtensions = [".ts", ".tsx", ".css"]
const moduleExtensions = [".ts", ".tsx", ".js", ".jsx", ".css", ".json"]

function exists(filePath) {
  return fs.existsSync(filePath)
}

function read(filePath) {
  return fs.readFileSync(filePath, "utf8")
}

function write(filePath, content) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true })
  fs.writeFileSync(filePath, content.endsWith("\n") ? content : `${content}\n`)
}

function remove(filePath) {
  if (!exists(filePath)) {
    return
  }

  fs.rmSync(filePath, { recursive: true, force: true })
}

function move(source, target) {
  if (!exists(source)) {
    return
  }

  if (exists(target)) {
    throw new Error(`Cannot move ${source}: target ${target} already exists`)
  }

  fs.mkdirSync(path.dirname(target), { recursive: true })
  fs.renameSync(source, target)
}

function walk(directory) {
  if (!exists(directory)) {
    return []
  }

  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name)

    return entry.isDirectory() ? walk(entryPath) : [entryPath]
  })
}

function sourceFiles() {
  return walk(sourceRoot).filter((filePath) =>
    sourceExtensions.some((extension) => filePath.endsWith(extension)),
  )
}

function replaceInFile(filePath, replacements) {
  if (!exists(filePath)) {
    return
  }

  let content = read(filePath)

  for (const [search, replacement] of replacements) {
    content = content.replaceAll(search, replacement)
  }

  write(filePath, content)
}

function replaceInSource(replacements) {
  for (const filePath of sourceFiles()) {
    replaceInFile(filePath, replacements)
  }
}

function mergeTypeScriptCssImports(entryPath, outputPath) {
  const content = read(entryPath)
  const imports = [...content.matchAll(/import\s+["'](.+?\.css)["'];?/g)].map(
    (match) => match[1],
  )

  if (imports.length < 2) {
    throw new Error(`Expected multiple CSS imports in ${entryPath}`)
  }

  const parts = imports.map((importPath) => {
    const cssPath = path.resolve(path.dirname(entryPath), importPath)

    if (!exists(cssPath)) {
      throw new Error(`Missing CSS file referenced by ${entryPath}: ${cssPath}`)
    }

    return read(cssPath).trim()
  })

  write(outputPath, `${parts.filter(Boolean).join("\n\n")}\n`)

  for (const importPath of imports) {
    remove(path.resolve(path.dirname(entryPath), importPath))
  }

  remove(entryPath)
}

function mergeCssImports(entryPath, outputPath) {
  const content = read(entryPath)
  const imports = [...content.matchAll(/@import\s+["'](.+?\.css)["'];?/g)].map(
    (match) => match[1],
  )

  if (imports.length < 2) {
    throw new Error(`Expected multiple CSS imports in ${entryPath}`)
  }

  const parts = imports.map((importPath) => {
    const cssPath = path.resolve(path.dirname(entryPath), importPath)

    if (!exists(cssPath)) {
      throw new Error(`Missing CSS file referenced by ${entryPath}: ${cssPath}`)
    }

    return read(cssPath).trim()
  })

  write(outputPath, `${parts.filter(Boolean).join("\n\n")}\n`)

  for (const importPath of imports) {
    remove(path.resolve(path.dirname(entryPath), importPath))
  }

  remove(entryPath)
}

function importSpecifiers(content) {
  const specifiers = new Set()
  const staticImportPattern = /\b(?:import|export)\s+(?:type\s+)?(?:[\w*{},\s]+?\s+from\s+)?["']([^"']+)["']/g
  const dynamicImportPattern = /\bimport\s*\(\s*["']([^"']+)["']\s*\)/g
  const cssImportPattern = /@import\s+["']([^"']+)["']/g

  for (const pattern of [staticImportPattern, dynamicImportPattern, cssImportPattern]) {
    for (const match of content.matchAll(pattern)) {
      specifiers.add(match[1])
    }
  }

  return [...specifiers]
}

function resolveImport(importer, specifier) {
  let basePath

  if (specifier.startsWith("@/")) {
    basePath = path.join(sourceRoot, specifier.slice(2))
  } else if (specifier.startsWith(".")) {
    basePath = path.resolve(path.dirname(importer), specifier)
  } else {
    return null
  }

  const candidates = [basePath]

  for (const extension of moduleExtensions) {
    candidates.push(`${basePath}${extension}`)
  }

  for (const extension of moduleExtensions) {
    candidates.push(path.join(basePath, `index${extension}`))
  }

  return candidates.find((candidate) => exists(candidate) && fs.statSync(candidate).isFile()) ?? null
}

function reachableSourceFiles() {
  const files = sourceFiles()
  const roots = files.filter((filePath) =>
    filePath === path.join(sourceRoot, "main.tsx") ||
    filePath.endsWith(".d.ts") ||
    /\.(?:test|spec)\.(?:ts|tsx)$/.test(filePath),
  )
  const reachable = new Set()
  const queue = [...roots]

  while (queue.length > 0) {
    const current = queue.shift()

    if (!current || reachable.has(current) || !exists(current)) {
      continue
    }

    reachable.add(current)

    for (const specifier of importSpecifiers(read(current))) {
      const resolved = resolveImport(current, specifier)

      if (resolved && !reachable.has(resolved)) {
        queue.push(resolved)
      }
    }
  }

  return reachable
}

function pruneUnreachableSource() {
  const reachable = reachableSourceFiles()

  for (const filePath of sourceFiles()) {
    if (!reachable.has(filePath)) {
      remove(filePath)
    }
  }
}

function removeEmptyDirectories(directory) {
  if (!exists(directory)) {
    return
  }

  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (entry.isDirectory()) {
      removeEmptyDirectories(path.join(directory, entry.name))
    }
  }

  if (directory !== sourceRoot && fs.readdirSync(directory).length === 0) {
    fs.rmdirSync(directory)
  }
}

function updatePackageConfiguration() {
  const packagePath = path.join(miniAppRoot, "package.json")
  const packageJson = JSON.parse(read(packagePath))

  packageJson.scripts.format = 'prettier --write "src/**/*.{ts,tsx,css}" "*.{js,json,ts,html,md}"'
  packageJson.scripts["format:check"] = 'prettier --check "src/**/*.{ts,tsx,css}" "*.{js,json,ts,html,md}"'

  write(packagePath, `${JSON.stringify(packageJson, null, 2)}\n`)

  write(
    path.join(miniAppRoot, ".prettierrc.json"),
    `${JSON.stringify(
      {
        printWidth: 100,
        semi: false,
        singleQuote: false,
        trailingComma: "all",
      },
      null,
      2,
    )}\n`,
  )

  write(
    path.join(miniAppRoot, ".prettierignore"),
    ["dist", "node_modules", "coverage", "public"].join("\n"),
  )

  write(
    path.join(miniAppRoot, ".gitignore"),
    [".env", "dist", "node_modules", "coverage"].join("\n"),
  )
}

function updateVerificationWorkflow() {
  const workflowPath = path.join(repositoryRoot, ".github/workflows/mini-app.yml")
  let workflow = read(workflowPath)

  if (!workflow.includes("- name: Check formatting")) {
    workflow = workflow.replace(
      "      - name: Lint\n        run: npm run lint\n",
      "      - name: Lint\n        run: npm run lint\n\n      - name: Check formatting\n        run: npm run format:check\n",
    )
  }

  write(workflowPath, workflow)
}

function cleanAppProvider() {
  const providerPath = path.join(sourceRoot, "app/providers/AppProvider.tsx")

  write(
    providerPath,
    `import {\n  createContext,\n  type ReactNode,\n  useCallback,\n  useContext,\n  useEffect,\n  useMemo,\n  useState,\n} from "react"\nimport { useTranslation } from "react-i18next"\nimport type { LocalizationLanguage } from "@/product-ui/model"\nimport type { CurrentContextResponse } from "@/shared/api/backendContracts"\nimport {\n  createAccount,\n  getCurrentContext,\n  initializeCurrentUser,\n  joinAccount,\n  leaveActiveAccount,\n  reactivateAccount,\n  selectActiveAccount,\n  updateCurrentLanguage,\n} from "@/shared/api/productApi"\nimport { handleError } from "@/shared/errors/errorHandler"\n\ntype AppState = {\n  context: CurrentContextResponse | null\n  loading: boolean\n  refresh(): Promise<CurrentContextResponse>\n  selectAccount(accountId: string): Promise<void>\n  registerAccount(nickname: string): Promise<void>\n  joinByCode(inviteCode: string): Promise<void>\n  leaveAccount(): Promise<void>\n  reactivate(accountId: string): Promise<void>\n  setLanguage(language: LocalizationLanguage): Promise<void>\n}\n\nconst AppContext = createContext<AppState | null>(null)\n\nexport function AppProvider({ children }: { children: ReactNode }) {\n  const { i18n } = useTranslation()\n  const [context, setContext] = useState<CurrentContextResponse | null>(null)\n  const [loading, setLoading] = useState(true)\n\n  const refresh = useCallback(async () => {\n    const next = await getCurrentContext()\n\n    setContext(next)\n    return next\n  }, [])\n\n  const bootstrap = useCallback(async () => {\n    setLoading(true)\n\n    try {\n      await initializeCurrentUser()\n      const next = await getCurrentContext()\n\n      setContext(next)\n      await i18n.changeLanguage(next.user.localizationLanguage.toLowerCase())\n    } finally {\n      setLoading(false)\n    }\n  }, [i18n])\n\n  const selectAccount = useCallback(\n    async (accountId: string) => {\n      await selectActiveAccount(accountId)\n      await refresh()\n    },\n    [refresh],\n  )\n\n  const registerAccount = useCallback(\n    async (nickname: string) => {\n      const created = await createAccount(nickname)\n\n      await selectActiveAccount(created.accountId)\n      await refresh()\n    },\n    [refresh],\n  )\n\n  const joinByCode = useCallback(\n    async (inviteCode: string) => {\n      const joined = await joinAccount(inviteCode)\n\n      await selectActiveAccount(joined.accountId)\n      await refresh()\n    },\n    [refresh],\n  )\n\n  const leaveAccount = useCallback(async () => {\n    await leaveActiveAccount()\n    await refresh()\n  }, [refresh])\n\n  const reactivate = useCallback(\n    async (accountId: string) => {\n      await reactivateAccount(accountId)\n      await selectActiveAccount(accountId)\n      await refresh()\n    },\n    [refresh],\n  )\n\n  const setLanguage = useCallback(\n    async (language: LocalizationLanguage) => {\n      await updateCurrentLanguage(language)\n      await i18n.changeLanguage(language.toLowerCase())\n      await refresh()\n    },\n    [i18n, refresh],\n  )\n\n  useEffect(() => {\n    const frame = window.requestAnimationFrame(() => {\n      void bootstrap().catch(handleError)\n    })\n\n    return () => window.cancelAnimationFrame(frame)\n  }, [bootstrap])\n\n  const value = useMemo<AppState>(\n    () => ({\n      context,\n      loading,\n      refresh,\n      selectAccount,\n      registerAccount,\n      joinByCode,\n      leaveAccount,\n      reactivate,\n      setLanguage,\n    }),\n    [\n      context,\n      loading,\n      refresh,\n      selectAccount,\n      registerAccount,\n      joinByCode,\n      leaveAccount,\n      reactivate,\n      setLanguage,\n    ],\n  )\n\n  return <AppContext.Provider value={value}>{children}</AppContext.Provider>\n}\n\nexport function useApp() {\n  const value = useContext(AppContext)\n\n  if (!value) {\n    throw new Error("AppProvider is missing")\n  }\n\n  return value\n}\n`,
  )
}

function cleanAuthGate() {
  const authGatePath = path.join(sourceRoot, "features/auth/ui/AuthGate.tsx")

  write(
    authGatePath,
    `import { type ReactNode, useEffect, useState } from "react"\nimport { useTranslation } from "react-i18next"\nimport { initializeAuth } from "@/features/auth/api/auth"\nimport { LoadingPage } from "@/pages/loading/LoadingPage"\nimport { handleError } from "@/shared/errors/errorHandler"\n\ntype AuthState = "loading" | "ready" | "telegram" | "error"\n\nexport function AuthGate({ children }: { children: ReactNode }) {\n  const { t } = useTranslation()\n  const [state, setState] = useState<AuthState>("loading")\n\n  useEffect(() => {\n    void initializeAuth()\n      .then(() => {\n        setState("ready")\n      })\n      .catch((error) => {\n        if (error instanceof Error && error.message === "TELEGRAM_REQUIRED") {\n          setState("telegram")\n          return\n        }\n\n        handleError(error)\n        setState("error")\n      })\n  }, [])\n\n  if (state === "ready") {\n    return children\n  }\n\n  if (state === "telegram") {\n    return <LoadingPage label={t("auth.telegramOnly")} />\n  }\n\n  if (state === "error") {\n    return <LoadingPage label={t("auth.error")} />\n  }\n\n  return <LoadingPage label={t("auth.authorizing")} />\n}\n`,
  )
}

function cleanAccountAreaContract() {
  const accountAreaPath = path.join(sourceRoot, "product-ui/features/AccountAreaFinal.tsx")

  replaceInFile(accountAreaPath, [
    ["import type { Account, Route } from \"../model\"", "import type { Route } from \"../model\""],
    ["  activeAccount: Account\n  setActiveAccount: (account: Account) => void\n  accounts: Account[]\n  setAccounts: (accounts: Account[]) => void\n", ""],
  ])

  const appPath = path.join(sourceRoot, "product-ui/AppFinal.tsx")
  let app = read(appPath)

  app = app.replace(
    /<AccountAreaFinal\s+routeId=\{current\.id\}[\s\S]*?back=\{back\}\s*\/>/,
    `<AccountAreaFinal\n                routeId={current.id}\n                params={current.params}\n                navigate={navigate}\n                back={back}\n              />`,
  )

  app = app.replace(
    /<AccountAreaFinal\s+routeId=\{route\.id\}[\s\S]*?back=\{back\}\s*\/>/,
    `<AccountAreaFinal\n        routeId={route.id}\n        params={route.params}\n        navigate={navigate}\n        back={back}\n      />`,
  )

  app = app.replace(/\nconst emptyAccount: Account = \{[\s\S]*?\n\}\n\nfunction RootNavigation/, "\nfunction RootNavigation")

  write(appPath, app)
}

function normalizeProductionNames() {
  const wrapperReplacements = [
    ["AppFinal", "ProductApp"],
    ["AccountAreaFinal", "AccountArea"],
    ["EventsAreaRoundFour", "EventsArea"],
    ["ModerationHomePolished", "ModerationArea"],
    ["PeopleFeatureFinal", "PeopleArea"],
    ["PersonalizationFeatureFinal", "PersonalizationArea"],
    ["StaffAreaOverhaul", "StaffArea"],
    ["CreatorFeatureRethink", "CreatorArea"],
  ]

  const productionReplacements = [
    ["AccountHomeProduction", "AccountHome"],
    ["AccountInvitesProduction", "AccountInvites"],
    ["AccountOnboardingProduction", "AccountOnboarding"],
    ["AccountCreateProduction", "AccountCreate"],
    ["AccountJoinProduction", "AccountJoin"],
    ["AccountSettingsProduction", "AccountSettings"],
    ["AccountWalletProduction", "AccountWallet"],
    ["BadgeAdminProduction", "BadgeAdmin"],
    ["BetAdminProduction", "BetAdmin"],
    ["BetsProduction", "Bets"],
    ["CreatorFeatureProduction", "CreatorFeature"],
    ["EventDetailProduction", "EventDetail"],
    ["EventsFeedProduction", "EventsFeed"],
    ["ModerationFeatureProduction", "ModerationFeature"],
    ["ModerationHomeProduction", "ModerationHome"],
    ["ModerationUserProduction", "ModerationUser"],
    ["PeopleFeatureProduction", "PeopleFeature"],
    ["PersonalizationFeatureProduction", "PersonalizationFeature"],
    ["PersonalizationReviewProduction", "PersonalizationReview"],
    ["RoleEditorProduction", "RoleEditor"],
    ["RolesOverviewProduction", "RolesOverview"],
    ["SubscriptionsProduction", "Subscriptions"],
    ["UserPrimeWalletProduction", "UserPrimeWallet"],
  ]

  const creatorWrapperPath = path.join(sourceRoot, "product-ui/features/CreatorFeatureRethink.tsx")
  replaceInFile(creatorWrapperPath, [["export function CreatorFeature", "export function CreatorArea"]])

  const appPath = path.join(sourceRoot, "product-ui/AppFinal.tsx")
  replaceInFile(appPath, [
    ["import { CreatorFeature } from \"./features/CreatorFeatureRethink\"", "import { CreatorArea } from \"./features/CreatorFeatureRethink\""],
    ["<CreatorFeature routeId=", "<CreatorArea routeId="],
  ])

  replaceInSource([...wrapperReplacements, ...productionReplacements])

  const renameMap = new Map([
    ["product-ui/AppFinal.tsx", "product-ui/ProductApp.tsx"],
    ["product-ui/features/AccountAreaFinal.tsx", "product-ui/features/AccountArea.tsx"],
    ["product-ui/features/CreatorFeatureRethink.tsx", "product-ui/features/CreatorArea.tsx"],
    ["product-ui/features/EventsAreaRoundFour.tsx", "product-ui/features/EventsArea.tsx"],
    ["product-ui/features/ModerationHomePolished.tsx", "product-ui/features/ModerationArea.tsx"],
    ["product-ui/features/PeopleFeatureFinal.tsx", "product-ui/features/PeopleArea.tsx"],
    ["product-ui/features/PersonalizationFeatureFinal.tsx", "product-ui/features/PersonalizationArea.tsx"],
    ["product-ui/features/StaffAreaOverhaul.tsx", "product-ui/features/StaffArea.tsx"],
    ["product-ui/features/AccountHomeProduction.tsx", "product-ui/features/AccountHome.tsx"],
    ["product-ui/features/AccountInvitesProduction.tsx", "product-ui/features/AccountInvites.tsx"],
    ["product-ui/features/AccountOnboardingProduction.tsx", "product-ui/features/AccountOnboarding.tsx"],
    ["product-ui/features/AccountSettingsProduction.tsx", "product-ui/features/AccountSettings.tsx"],
    ["product-ui/features/AccountWalletProduction.tsx", "product-ui/features/AccountWallet.tsx"],
    ["product-ui/features/BadgeAdminProduction.tsx", "product-ui/features/BadgeAdmin.tsx"],
    ["product-ui/features/BetAdminProduction.tsx", "product-ui/features/BetAdmin.tsx"],
    ["product-ui/features/BetsProduction.tsx", "product-ui/features/Bets.tsx"],
    ["product-ui/features/CreatorFeatureProduction.tsx", "product-ui/features/CreatorFeature.tsx"],
    ["product-ui/features/EventDetailProduction.tsx", "product-ui/features/EventDetail.tsx"],
    ["product-ui/features/EventsFeedProduction.tsx", "product-ui/features/EventsFeed.tsx"],
    ["product-ui/features/ModerationFeatureProduction.tsx", "product-ui/features/ModerationFeature.tsx"],
    ["product-ui/features/PeopleFeatureProduction.tsx", "product-ui/features/PeopleFeature.tsx"],
    ["product-ui/features/PersonalizationFeatureProduction.tsx", "product-ui/features/PersonalizationFeature.tsx"],
    ["product-ui/features/PersonalizationReviewProduction.tsx", "product-ui/features/PersonalizationReview.tsx"],
    ["product-ui/features/RoleEditorProduction.tsx", "product-ui/features/RoleEditor.tsx"],
    ["product-ui/features/RolesOverviewProduction.tsx", "product-ui/features/RolesOverview.tsx"],
    ["product-ui/features/SubscriptionsProduction.tsx", "product-ui/features/Subscriptions.tsx"],
    ["product-ui/features/UserPrimeWalletProduction.tsx", "product-ui/features/UserPrimeWallet.tsx"],
  ])

  for (const [source, target] of renameMap) {
    move(path.join(sourceRoot, source), path.join(sourceRoot, target))
  }
}

function assertCleanSource() {
  const files = sourceFiles().map((filePath) => path.relative(sourceRoot, filePath))
  const forbiddenNames = files.filter((filePath) =>
    /(?:mock|final|polished|rethink|overhaul|round(?:-|_)?(?:three|four|five|six|seven)|production|fixes|revision|product-v\d)/i.test(
      filePath,
    ),
  )

  if (forbiddenNames.length > 0) {
    throw new Error(`Historical/mock production source remains:\n${forbiddenNames.join("\n")}`)
  }

  const mockImports = sourceFiles().flatMap((filePath) => {
    const matches = read(filePath).match(/(?:mockData|MockStore|moderationMockData|roleMockStore)/g)

    return matches ? [path.relative(sourceRoot, filePath)] : []
  })

  if (mockImports.length > 0) {
    throw new Error(`Mock references remain in production source:\n${mockImports.join("\n")}`)
  }
}

remove(path.join(repositoryRoot, "apps/mini-app-mockup"))
remove(path.join(repositoryRoot, ".github/workflows/mini-app-mockup.yml"))
remove(path.join(miniAppRoot, "dist"))
remove(path.join(miniAppRoot, "reference-ui.jpeg"))
remove(path.join(sourceRoot, "product-ui/vite-env.d.ts"))

mergeTypeScriptCssImports(
  path.join(sourceRoot, "product-ui/productStyles.ts"),
  path.join(sourceRoot, "product-ui/product.css"),
)
mergeCssImports(
  path.join(sourceRoot, "shared/styles/index.css"),
  path.join(sourceRoot, "shared/styles/global.css"),
)

replaceInFile(path.join(sourceRoot, "app/App.tsx"), [
  ["import \"@/product-ui/productStyles\"", "import \"@/product-ui/product.css\""],
])
replaceInFile(path.join(sourceRoot, "main.tsx"), [
  ["import \"@/shared/styles/index.css\"", "import \"@/shared/styles/global.css\""],
])

cleanAccountAreaContract()
pruneUnreachableSource()
cleanAppProvider()
cleanAuthGate()
normalizeProductionNames()
pruneUnreachableSource()
removeEmptyDirectories(sourceRoot)
updatePackageConfiguration()
updateVerificationWorkflow()
assertCleanSource()

console.log(`Frontend cleanup completed. Reachable source files: ${sourceFiles().length}`)
