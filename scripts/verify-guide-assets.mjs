import { readdir, readFile, stat } from "node:fs/promises"
import path from "node:path"
import process from "node:process"
import { fileURLToPath } from "node:url"

const repositoryRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..")
const guideRoot = process.argv[2]
  ? path.resolve(repositoryRoot, process.argv[2])
  : path.join(repositoryRoot, "apps/mini-app/public/guides")

const expectedDimensions = new Map([
  ["entities/case-icon.webp", [32, 32]],
  ["entities/comet-atlas.webp", [192, 96]],
  ["entities/npc-atlas.webp", [256, 3584]],
  ["entities/rebirth-emblem-atlas.webp", [960, 64]],
  ["entities/weapon-atlas.webp", [1536, 3584]],
  ["locations/location-dungeon-hall.webp", [694, 160]],
])

const expectedLocationNames = [
  "location-00-starter-village.webp",
  "location-01-dark-forest.webp",
  "location-02-pirate-ship.webp",
  "location-03-shinobi.webp",
  "location-04-polar-tundra.webp",
  "location-05-aether-city.webp",
  "location-06-hell.webp",
  "location-07-ancient-sands.webp",
  "location-08-enchanted-forest.webp",
  "location-09-magic-mine.webp",
  "location-10-secret-land.webp",
  "location-11-container-yard.webp",
  "location-12-viking-valley.webp",
  "location-13-abandoned-prison.webp",
  "location-14-oasis.webp",
  "location-15-canyon.webp",
  "location-16-obelisk-meadows.webp",
  "location-17-sunken-temple.webp",
  "location-18-port.webp",
  "location-19-blazing-abyss.webp",
  "location-20-sea-castle.webp",
  "location-21-taiping-tower.webp",
  "location-22-mushroom-world.webp",
  "location-23-palisade.webp",
  "location-24-robot-village.webp",
  "location-25-cosmodriver.webp",
  "location-26-cursed-mall.webp",
  "location-27-bio-lab.webp",
  "location-28-waterfall.webp",
]

for (const name of expectedLocationNames) {
  expectedDimensions.set(`locations/${name}`, [504, 176])
}

const webpPaths = await collectWebpPaths(guideRoot)
const failures = []

for (const [relativePath, dimensions] of expectedDimensions) {
  const absolutePath = path.join(guideRoot, relativePath)

  try {
    const info = await stat(absolutePath)
    if (!info.isFile()) {
      failures.push(`${relativePath}: expected a regular file`)
    }
  } catch {
    failures.push(`${relativePath}: missing required guide artwork`)
    continue
  }

  const buffer = await readFile(absolutePath)

  try {
    const actualDimensions = validateWebp(buffer)

    if (actualDimensions[0] !== dimensions[0] || actualDimensions[1] !== dimensions[1]) {
      failures.push(
        `${relativePath}: expected ${dimensions[0]}x${dimensions[1]}, got ${actualDimensions[0]}x${actualDimensions[1]}`,
      )
    }
  } catch (error) {
    failures.push(`${relativePath}: ${error.message}`)
  }
}

for (const relativePath of webpPaths) {
  if (!expectedDimensions.has(relativePath) && /(atlas|location-|case-icon)/.test(relativePath)) {
    const buffer = await readFile(path.join(guideRoot, relativePath))

    try {
      validateWebp(buffer)
    } catch (error) {
      failures.push(`${relativePath}: ${error.message}`)
    }
  }

  if (/(^|\/)(temp|tmp|stage|staging|generated)(\/|[-_.])/i.test(relativePath)) {
    failures.push(`${relativePath}: temporary or staging guide asset must not be committed`)
  }
}

if (failures.length > 0) {
  console.error("Guide asset verification failed:")
  for (const failure of failures) {
    console.error(`- ${failure}`)
  }
  process.exit(1)
}

console.log(`Verified ${expectedDimensions.size} required guide WebP assets.`)

async function collectWebpPaths(directory, relativeDirectory = "") {
  const entries = await readdir(directory, { withFileTypes: true })
  const files = []

  for (const entry of entries) {
    const relativePath = path.posix.join(relativeDirectory, entry.name)
    const absolutePath = path.join(directory, entry.name)

    if (entry.isDirectory()) {
      files.push(...(await collectWebpPaths(absolutePath, relativePath)))
    } else if (entry.isFile() && entry.name.endsWith(".webp")) {
      files.push(relativePath)
    }
  }

  return files
}

function validateWebp(buffer) {
  if (buffer.length < 20) {
    throw new Error("file is too small to be a WebP container")
  }

  if (buffer.toString("ascii", 0, 4) !== "RIFF" || buffer.toString("ascii", 8, 12) !== "WEBP") {
    throw new Error("missing RIFF/WEBP signature")
  }

  const declaredSize = buffer.readUInt32LE(4) + 8
  if (declaredSize !== buffer.length) {
    throw new Error(`truncated or overlong RIFF container: declared ${declaredSize}, got ${buffer.length}`)
  }

  let dimensions = null
  let offset = 12

  while (offset < buffer.length) {
    if (offset + 8 > buffer.length) {
      throw new Error("truncated WebP chunk header")
    }

    const chunkType = buffer.toString("ascii", offset, offset + 4)
    const chunkSize = buffer.readUInt32LE(offset + 4)
    const dataOffset = offset + 8
    const dataEnd = dataOffset + chunkSize

    if (dataEnd > buffer.length) {
      throw new Error(`${chunkType} chunk exceeds the RIFF container`)
    }

    dimensions ??= readDimensions(buffer, chunkType, dataOffset, chunkSize)
    offset = dataEnd + (chunkSize % 2)
  }

  if (offset !== buffer.length) {
    throw new Error("invalid WebP chunk padding")
  }

  if (dimensions === null) {
    throw new Error("no supported WebP image chunk found")
  }

  return dimensions
}

function readDimensions(buffer, chunkType, offset, chunkSize) {
  if (chunkType === "VP8X" && chunkSize >= 10) {
    return [readUInt24LE(buffer, offset + 4) + 1, readUInt24LE(buffer, offset + 7) + 1]
  }

  if (chunkType === "VP8 " && chunkSize >= 10) {
    if (buffer[offset + 3] !== 0x9d || buffer[offset + 4] !== 0x01 || buffer[offset + 5] !== 0x2a) {
      throw new Error("invalid VP8 frame header")
    }

    return [buffer.readUInt16LE(offset + 6) & 0x3fff, buffer.readUInt16LE(offset + 8) & 0x3fff]
  }

  if (chunkType === "VP8L" && chunkSize >= 5) {
    if (buffer[offset] !== 0x2f) {
      throw new Error("invalid VP8L frame header")
    }

    const bits = buffer.readUInt32LE(offset + 1)
    return [(bits & 0x3fff) + 1, ((bits >>> 14) & 0x3fff) + 1]
  }

  return null
}

function readUInt24LE(buffer, offset) {
  return buffer[offset] | (buffer[offset + 1] << 8) | (buffer[offset + 2] << 16)
}
