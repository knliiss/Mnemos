type Vector3 = readonly [number, number, number]

type ProjectedVertex = {
  x: number
  y: number
  depth: number
}

type TextureRegion = {
  x: number
  y: number
}

type HeadFace = {
  vertices: readonly Vector3[]
  normal: Vector3
  baseTexture: TextureRegion
  overlayTexture: TextureRegion
  shade: number
}

const HEAD_FACE_SIZE = 8
const HEAD_RENDER_SIZE = 96
const HEAD_YAW_DEGREES = -30
const HEAD_PITCH_DEGREES = 20
const BASE_HALF_SIZE = 1
const OVERLAY_HALF_SIZE = 1.075
const PROJECTION_SCALE = HEAD_RENDER_SIZE * 0.25

const STEVE_FACE = [
  ["#6f4b32", "#704a31", "#62402d", "#5a3928", "#5b3a29", "#68452f", "#745039", "#76513a"],
  ["#704a31", "#9a6845", "#a6714b", "#a86f4a", "#a56b48", "#a16a47", "#96613f", "#704a31"],
  ["#9d6946", "#ad7650", "#b57b54", "#b77e56", "#b57a53", "#b07952", "#a66f4b", "#95603f"],
  ["#a46d49", "#ece7d8", "#5d7187", "#ad7650", "#ac744e", "#5d7187", "#ece7d8", "#9b6544"],
  ["#a26b48", "#f4efdf", "#3c5268", "#ad754f", "#aa724d", "#3c5268", "#f4efdf", "#986241"],
  ["#9f6845", "#aa724c", "#aa714c", "#784936", "#784936", "#a66d49", "#a36b47", "#95603f"],
  ["#97613f", "#a56c48", "#774936", "#7c4b37", "#7c4b37", "#754532", "#9e6644", "#8f5b3d"],
  ["#68432f", "#8d593b", "#9b6341", "#9f6643", "#9d6542", "#965f40", "#875337", "#64402d"],
] as const

const HEAD_FACES: readonly HeadFace[] = [
  {
    vertices: [
      [-1, 1, 1],
      [1, 1, 1],
      [1, -1, 1],
      [-1, -1, 1],
    ],
    normal: [0, 0, 1],
    baseTexture: { x: 8, y: 8 },
    overlayTexture: { x: 40, y: 8 },
    shade: 0,
  },
  {
    vertices: [
      [1, 1, 1],
      [1, 1, -1],
      [1, -1, -1],
      [1, -1, 1],
    ],
    normal: [1, 0, 0],
    baseTexture: { x: 0, y: 8 },
    overlayTexture: { x: 32, y: 8 },
    shade: 0.14,
  },
  {
    vertices: [
      [-1, 1, -1],
      [-1, 1, 1],
      [-1, -1, 1],
      [-1, -1, -1],
    ],
    normal: [-1, 0, 0],
    baseTexture: { x: 16, y: 8 },
    overlayTexture: { x: 48, y: 8 },
    shade: 0.18,
  },
  {
    vertices: [
      [1, 1, -1],
      [-1, 1, -1],
      [-1, -1, -1],
      [1, -1, -1],
    ],
    normal: [0, 0, -1],
    baseTexture: { x: 24, y: 8 },
    overlayTexture: { x: 56, y: 8 },
    shade: 0.2,
  },
  {
    vertices: [
      [-1, 1, -1],
      [1, 1, -1],
      [1, 1, 1],
      [-1, 1, 1],
    ],
    normal: [0, 1, 0],
    baseTexture: { x: 8, y: 0 },
    overlayTexture: { x: 40, y: 0 },
    shade: 0,
  },
  {
    vertices: [
      [-1, -1, 1],
      [1, -1, 1],
      [1, -1, -1],
      [-1, -1, -1],
    ],
    normal: [0, -1, 0],
    baseTexture: { x: 16, y: 0 },
    overlayTexture: { x: 48, y: 0 },
    shade: 0.22,
  },
]

export function renderMinecraftHead(
  canvas: HTMLCanvasElement,
  image: CanvasImageSource,
  textureScale: number,
) {
  const context = canvas.getContext("2d")

  if (!context) {
    return false
  }

  canvas.width = HEAD_RENDER_SIZE
  canvas.height = HEAD_RENDER_SIZE
  context.clearRect(0, 0, HEAD_RENDER_SIZE, HEAD_RENDER_SIZE)
  context.imageSmoothingEnabled = false

  renderHeadLayer(context, image, textureScale, BASE_HALF_SIZE, false)
  renderHeadLayer(context, image, textureScale, OVERLAY_HALF_SIZE, true)

  return true
}

export function createSteveHeadTexture() {
  const canvas = document.createElement("canvas")
  canvas.width = 64
  canvas.height = 64

  const context = canvas.getContext("2d")
  if (!context) {
    return canvas
  }

  context.imageSmoothingEnabled = false
  context.fillStyle = "#a8704c"
  context.fillRect(0, 0, 32, 16)

  context.fillStyle = "#6b472f"
  context.fillRect(0, 0, 32, 8)
  context.fillRect(0, 8, 8, 3)
  context.fillRect(16, 8, 8, 2)
  context.fillRect(24, 8, 8, 3)

  STEVE_FACE.forEach((row, y) => {
    row.forEach((color, x) => {
      context.fillStyle = color
      context.fillRect(8 + x, 8 + y, 1, 1)
    })
  })

  return canvas
}

function renderHeadLayer(
  context: CanvasRenderingContext2D,
  image: CanvasImageSource,
  textureScale: number,
  halfSize: number,
  overlay: boolean,
) {
  const visibleFaces = HEAD_FACES.map((face) => {
    const vertices = face.vertices.map((vertex) => projectVertex(scaleVertex(vertex, halfSize)))
    const normal = rotateVector(face.normal)
    const averageDepth = vertices.reduce((sum, vertex) => sum + vertex.depth, 0) / vertices.length

    return {
      face,
      vertices,
      normal,
      averageDepth,
    }
  })
    .filter(({ normal }) => normal[2] > 0.001)
    .sort((left, right) => left.averageDepth - right.averageDepth)

  visibleFaces.forEach(({ face, vertices }) => {
    const region = overlay ? face.overlayTexture : face.baseTexture

    drawTextureFace(context, image, textureScale, region, vertices)

    if (!overlay && face.shade > 0) {
      shadeFace(context, vertices, face.shade)
    }
  })
}

function drawTextureFace(
  context: CanvasRenderingContext2D,
  image: CanvasImageSource,
  textureScale: number,
  region: TextureRegion,
  vertices: ProjectedVertex[],
) {
  const [topLeft, topRight, , bottomLeft] = vertices

  context.save()
  clipFace(context, vertices)
  context.imageSmoothingEnabled = false
  context.setTransform(
    (topRight.x - topLeft.x) / HEAD_FACE_SIZE,
    (topRight.y - topLeft.y) / HEAD_FACE_SIZE,
    (bottomLeft.x - topLeft.x) / HEAD_FACE_SIZE,
    (bottomLeft.y - topLeft.y) / HEAD_FACE_SIZE,
    topLeft.x,
    topLeft.y,
  )
  context.drawImage(
    image,
    region.x * textureScale,
    region.y * textureScale,
    HEAD_FACE_SIZE * textureScale,
    HEAD_FACE_SIZE * textureScale,
    0,
    0,
    HEAD_FACE_SIZE,
    HEAD_FACE_SIZE,
  )
  context.restore()
}

function shadeFace(
  context: CanvasRenderingContext2D,
  vertices: ProjectedVertex[],
  opacity: number,
) {
  context.save()
  clipFace(context, vertices)
  context.fillStyle = `rgba(0, 0, 0, ${opacity})`
  context.fillRect(0, 0, HEAD_RENDER_SIZE, HEAD_RENDER_SIZE)
  context.restore()
}

function clipFace(context: CanvasRenderingContext2D, vertices: ProjectedVertex[]) {
  context.beginPath()
  context.moveTo(vertices[0].x, vertices[0].y)

  vertices.slice(1).forEach((vertex) => {
    context.lineTo(vertex.x, vertex.y)
  })

  context.closePath()
  context.clip()
}

function scaleVertex(vertex: Vector3, scale: number): Vector3 {
  return [vertex[0] * scale, vertex[1] * scale, vertex[2] * scale]
}

function projectVertex(vertex: Vector3): ProjectedVertex {
  const rotated = rotateVector(vertex)

  return {
    x: HEAD_RENDER_SIZE / 2 + rotated[0] * PROJECTION_SCALE,
    y: HEAD_RENDER_SIZE / 2 - rotated[1] * PROJECTION_SCALE,
    depth: rotated[2],
  }
}

function rotateVector(vector: Vector3): Vector3 {
  const yaw = degreesToRadians(HEAD_YAW_DEGREES)
  const pitch = degreesToRadians(HEAD_PITCH_DEGREES)

  const yawX = Math.cos(yaw) * vector[0] + Math.sin(yaw) * vector[2]
  const yawZ = -Math.sin(yaw) * vector[0] + Math.cos(yaw) * vector[2]

  const pitchY = Math.cos(pitch) * vector[1] - Math.sin(pitch) * yawZ
  const pitchZ = Math.sin(pitch) * vector[1] + Math.cos(pitch) * yawZ

  return [yawX, pitchY, pitchZ]
}

function degreesToRadians(degrees: number) {
  return (degrees * Math.PI) / 180
}
