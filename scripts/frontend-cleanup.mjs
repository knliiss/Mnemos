import fs from "node:fs"
import path from "node:path"

const repositoryRoot = process.cwd()
const sourceRoot = path.join(repositoryRoot, "apps/mini-app/src")
const historicalPattern = /(?:final|polished|redesign|(?:^|-)v[2-9](?:-|$))/i

function walk(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name)

    return entry.isDirectory() ? walk(entryPath) : [entryPath]
  })
}

const files = walk(sourceRoot).filter((filePath) => /\.(?:ts|tsx|css)$/.test(filePath))
const usages = new Map()

for (const filePath of files) {
  const content = fs.readFileSync(filePath, "utf8")
  const tokens = content.match(/[A-Za-z_][A-Za-z0-9_-]*/g) ?? []

  for (const token of tokens) {
    if (!historicalPattern.test(token)) {
      continue
    }

    const relativePath = path.relative(repositoryRoot, filePath)
    const paths = usages.get(token) ?? new Set()

    paths.add(relativePath)
    usages.set(token, paths)
  }
}

const sorted = [...usages.entries()].sort(([left], [right]) => left.localeCompare(right))

console.log(`Historical frontend tokens: ${sorted.length}`)

for (const [token, paths] of sorted) {
  console.log(`${token}\t${[...paths].sort().join(", ")}`)
}
