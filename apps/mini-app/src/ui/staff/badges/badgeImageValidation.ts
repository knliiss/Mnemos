const PNG_SIGNATURE = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]

export const MAX_BADGE_IMAGE_BYTES = 256 * 1024
export const MIN_BADGE_IMAGE_DIMENSION = 64
export const MAX_BADGE_IMAGE_DIMENSION = 512

const PNG_HEADER_SIZE = 24
const WIDTH_OFFSET = 16
const HEIGHT_OFFSET = 20

export async function validateBadgeImageFile(file: File): Promise<boolean> {
  if (file.type !== "image/png" || file.size === 0 || file.size > MAX_BADGE_IMAGE_BYTES) {
    return false
  }

  const header = new Uint8Array(await file.slice(0, PNG_HEADER_SIZE).arrayBuffer())

  return validateBadgePngHeader(header)
}

export function validateBadgePngHeader(header: Uint8Array): boolean {
  if (header.byteLength < PNG_HEADER_SIZE) {
    return false
  }

  const signatureValid = PNG_SIGNATURE.every((value, index) => header[index] === value)

  if (!signatureValid) {
    return false
  }

  const view = new DataView(header.buffer, header.byteOffset, header.byteLength)
  const width = view.getUint32(WIDTH_OFFSET)
  const height = view.getUint32(HEIGHT_OFFSET)

  return (
    width === height &&
    width >= MIN_BADGE_IMAGE_DIMENSION &&
    width <= MAX_BADGE_IMAGE_DIMENSION
  )
}
