import fs from "node:fs"
import path from "node:path"
import { PurgeCSS } from "purgecss"

const repositoryRoot = process.cwd()
const miniAppRoot = path.join(repositoryRoot, "apps/mini-app")
const sourceRoot = path.join(miniAppRoot, "src")
const productCssPath = path.join(sourceRoot, "product-ui/product.css")
const globalCssPath = path.join(sourceRoot, "shared/styles/global.css")

function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name)

    return entry.isDirectory() ? walk(entryPath) : [entryPath]
  })
}

function sourceFiles() {
  return walk(sourceRoot).filter((filePath) => /\.(?:ts|tsx)$/.test(filePath))
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")
}

function collectDynamicClassPrefixes(files) {
  const prefixes = new Set()

  for (const filePath of files) {
    const content = fs.readFileSync(filePath, "utf8")

    for (const templateMatch of content.matchAll(/`([\s\S]*?\$\{[\s\S]*?)`/g)) {
      const template = templateMatch[1]
      const staticParts = template.split(/\$\{[\s\S]*?\}/g)

      for (const part of staticParts) {
        const tokens = part.match(/[A-Za-z_][A-Za-z0-9_-]*-+/g) ?? []

        for (const token of tokens) {
          if (token.endsWith("-") && token.length >= 3) {
            prefixes.add(token)
          }
        }
      }
    }
  }

  return [...prefixes]
}

async function purgeStylesheet(cssPath, contentPaths, dynamicPrefixes) {
  const beforeBytes = fs.statSync(cssPath).size
  const safelist = dynamicPrefixes.map((prefix) => new RegExp(`^${escapeRegExp(prefix)}`))
  const [result] = await new PurgeCSS().purge({
    content: contentPaths,
    css: [cssPath],
    defaultExtractor: (content) => content.match(/[A-Za-z0-9_:/-]+/g) ?? [],
    safelist: {
      greedy: safelist,
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

const files = sourceFiles()
const dynamicPrefixes = collectDynamicClassPrefixes(files)
const contentPaths = [
  path.join(miniAppRoot, "index.html"),
  ...files,
]

console.log(`Safelisting ${dynamicPrefixes.length} dynamic class prefixes`)

await purgeStylesheet(productCssPath, contentPaths, dynamicPrefixes)
await purgeStylesheet(globalCssPath, contentPaths, dynamicPrefixes)
