#!/usr/bin/env python3
from __future__ import annotations

import concurrent.futures
import io
import json
import math
import os
import re
import subprocess
import tempfile
from pathlib import Path
from typing import Iterable

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MODEL_PATH = ROOT / "scripts" / "sao-artwork" / "pet-model.json"
ATLAS_PATH = ROOT / "apps" / "mini-app" / "public" / "event-icons" / "sao-pets.webp"
TYPESCRIPT_PATH = (
    ROOT
    / "apps"
    / "mini-app"
    / "src"
    / "ui"
    / "components"
    / "events"
    / "saoPetArtwork.ts"
)

LANG_MANIFEST_URL = "https://webdata.c7x.dev/client/lang.json"
PET_SKIN_BASE_URL = "https://rustorage.c7x.dev/butowka/sao/global/img/pet"
PET_ITEM_KEY = re.compile(r"^sao\.item\.(pet_l(\d+)_(\d+))\.name$")

CELL_SIZE = 64
ATLAS_COLUMNS = 16
RENDER_SIZE = 128
RENDER_MARGIN = 10


def fetch_bytes(url: str) -> bytes:
    result = subprocess.run(
        ["curl", "--fail", "--silent", "--show-error", "--location", url],
        check=True,
        capture_output=True,
    )
    return result.stdout


def fetch_json(url: str) -> dict:
    return json.loads(fetch_bytes(url).decode("utf-8"))


def discover_pet_ids() -> list[str]:
    manifest = fetch_json(LANG_MANIFEST_URL)
    russian = next(entry for entry in manifest["langs"] if entry.get("lang") == "ru_RU")
    sao = next(pie for pie in russian["pies"] if pie.get("pieKey") == "sao")
    properties = fetch_json(sao["url"]).get("properties", {})

    pet_ids = []
    for key in properties:
        match = PET_ITEM_KEY.match(key)
        if match:
            pet_ids.append(match.group(1))

    return sorted(set(pet_ids), key=pet_sort_key)


def pet_sort_key(item_id: str) -> tuple[int, int, str]:
    match = re.fullmatch(r"pet_l(\d+)_(\d+)", item_id)
    if not match:
        return (10**9, 10**9, item_id)
    return (int(match.group(1)), int(match.group(2)), item_id)


def download_skin(item_id: str, destination: Path) -> None:
    url = f"{PET_SKIN_BASE_URL}/{item_id}.png"
    raw = fetch_bytes(url)

    image = Image.open(io.BytesIO(raw))
    if image.size != (64, 64):
        raise RuntimeError(f"Unexpected pet skin size for {item_id}: {image.size}")

    destination.write_bytes(raw)


def download_skins(item_ids: Iterable[str], directory: Path) -> None:
    item_ids = list(item_ids)
    worker_count = min(24, max(4, (os.cpu_count() or 2) * 4))

    with concurrent.futures.ThreadPoolExecutor(max_workers=worker_count) as executor:
        futures = {
            executor.submit(download_skin, item_id, directory / f"{item_id}.png"): item_id
            for item_id in item_ids
        }
        for future in concurrent.futures.as_completed(futures):
            item_id = futures[future]
            try:
                future.result()
            except Exception as exc:
                raise RuntimeError(f"Failed to download pet skin {item_id}") from exc


def translation(vector: np.ndarray) -> np.ndarray:
    matrix = np.eye(4, dtype=float)
    matrix[:3, 3] = vector
    return matrix


def rotation_x(degrees: float) -> np.ndarray:
    angle = math.radians(degrees)
    cosine, sine = math.cos(angle), math.sin(angle)
    return np.array(
        [[1, 0, 0, 0], [0, cosine, -sine, 0], [0, sine, cosine, 0], [0, 0, 0, 1]],
        dtype=float,
    )


def rotation_y(degrees: float) -> np.ndarray:
    angle = math.radians(degrees)
    cosine, sine = math.cos(angle), math.sin(angle)
    return np.array(
        [[cosine, 0, sine, 0], [0, 1, 0, 0], [-sine, 0, cosine, 0], [0, 0, 0, 1]],
        dtype=float,
    )


def rotation_z(degrees: float) -> np.ndarray:
    angle = math.radians(degrees)
    cosine, sine = math.cos(angle), math.sin(angle)
    return np.array(
        [[cosine, -sine, 0, 0], [sine, cosine, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]],
        dtype=float,
    )


def rotation_xyz(rotation: list[float] | None) -> np.ndarray:
    x, y, z = (rotation or [0, 0, 0])[:3]
    return rotation_z(z) @ rotation_y(y) @ rotation_x(x)


def build_bone_matrices(bones: list[dict]) -> dict[str, np.ndarray]:
    by_name = {bone["name"]: bone for bone in bones}
    cache: dict[str, np.ndarray] = {}

    def resolve(name: str) -> np.ndarray:
        if name in cache:
            return cache[name]

        bone = by_name[name]
        parent = np.eye(4, dtype=float)
        if parent_name := bone.get("parent"):
            parent = resolve(parent_name)

        pivot = np.asarray(bone.get("pivot", [0, 0, 0]), dtype=float)
        matrix = (
            parent
            @ translation(pivot)
            @ rotation_xyz(bone.get("rotation"))
            @ translation(-pivot)
        )
        cache[name] = matrix
        return matrix

    for bone_name in by_name:
        resolve(bone_name)

    return cache

def face_vertices(name: str, low: np.ndarray, high: np.ndarray) -> list[tuple[float, float, float]]:
    x0, y0, z0 = low
    x1, y1, z1 = high
    faces = {
        "north": [(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)],
        "south": [(x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)],
        "east": [(x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)],
        "west": [(x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)],
        "up": [(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)],
        "down": [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)],
    }
    return faces[name]


def face_uv(face: dict) -> np.ndarray:
    u0, v0 = map(float, face.get("uv", [0, 0]))
    width, height = map(float, face.get("uv_size", [0, 0]))
    corners = [
        (u0, v0),
        (u0 + width, v0),
        (u0 + width, v0 + height),
        (u0, v0 + height),
    ]

    rotation = int(face.get("uv_rotation", 0)) % 360
    if rotation == 90:
        corners = [corners[3], corners[0], corners[1], corners[2]]
    elif rotation == 180:
        corners = [corners[2], corners[3], corners[0], corners[1]]
    elif rotation == 270:
        corners = [corners[1], corners[2], corners[3], corners[0]]

    return np.asarray(corners, dtype=float)


def prepare_faces(model: dict) -> list[tuple[np.ndarray, np.ndarray]]:
    geometry = model["minecraft:geometry"][0]
    bones = geometry["bones"]
    bone_matrices = build_bone_matrices(bones)
    prepared = []

    for bone in bones:
        bone_matrix = bone_matrices[bone["name"]]
        for cube in bone.get("cubes", []):
            origin = np.asarray(cube["origin"], dtype=float)
            size = np.asarray(cube["size"], dtype=float)
            inflate = float(cube.get("inflate", 0.0))
            low = origin - inflate
            high = origin + size + inflate

            cube_matrix = bone_matrix
            if cube_rotation := cube.get("rotation"):
                pivot = np.asarray(cube.get("pivot", cube["origin"]), dtype=float)
                cube_matrix = (
                    bone_matrix
                    @ translation(pivot)
                    @ rotation_xyz(cube_rotation)
                    @ translation(-pivot)
                )

            for face_name, uv_definition in cube.get("uv", {}).items():
                if face_name not in {"north", "south", "east", "west", "up", "down"}:
                    continue

                vertices = np.asarray(
                    [(*point, 1.0) for point in face_vertices(face_name, low, high)],
                    dtype=float,
                )
                transformed = (cube_matrix @ vertices.T).T[:, :3]
                prepared.append((transformed, face_uv(uv_definition)))

    return prepared


def scene_transform(faces: list[tuple[np.ndarray, np.ndarray]]) -> list[tuple[np.ndarray, np.ndarray]]:
    all_vertices = np.concatenate([vertices for vertices, _ in faces], axis=0)
    center = (all_vertices.min(axis=0) + all_vertices.max(axis=0)) / 2
    matrix = translation(center) @ rotation_y(-28) @ rotation_x(10) @ translation(-center)

    transformed = []
    for vertices, uv in faces:
        homogeneous = np.column_stack((vertices, np.ones(len(vertices))))
        transformed.append(((matrix @ homogeneous.T).T[:, :3], uv))
    return transformed


def rasterize_triangle(
    output: np.ndarray,
    depth_buffer: np.ndarray,
    texture: np.ndarray,
    points: np.ndarray,
    uv: np.ndarray,
    shade: float,
) -> None:
    min_x = max(0, int(math.floor(points[:, 0].min())))
    max_x = min(output.shape[1] - 1, int(math.ceil(points[:, 0].max())))
    min_y = max(0, int(math.floor(points[:, 1].min())))
    max_y = min(output.shape[0] - 1, int(math.ceil(points[:, 1].max())))
    if max_x < min_x or max_y < min_y:
        return

    a, b, c = points[:, :2]
    denominator = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
    if abs(denominator) < 1e-9:
        return

    xs, ys = np.meshgrid(
        np.arange(min_x, max_x + 1, dtype=float) + 0.5,
        np.arange(min_y, max_y + 1, dtype=float) + 0.5,
    )
    weight_a = ((b[1] - c[1]) * (xs - c[0]) + (c[0] - b[0]) * (ys - c[1])) / denominator
    weight_b = ((c[1] - a[1]) * (xs - c[0]) + (a[0] - c[0]) * (ys - c[1])) / denominator
    weight_c = 1.0 - weight_a - weight_b
    inside = (weight_a >= -1e-6) & (weight_b >= -1e-6) & (weight_c >= -1e-6)
    if not inside.any():
        return

    depth = weight_a * points[0, 2] + weight_b * points[1, 2] + weight_c * points[2, 2]
    region_depth = depth_buffer[min_y : max_y + 1, min_x : max_x + 1]
    visible = inside & (depth < region_depth)
    if not visible.any():
        return

    u = weight_a * uv[0, 0] + weight_b * uv[1, 0] + weight_c * uv[2, 0]
    v = weight_a * uv[0, 1] + weight_b * uv[1, 1] + weight_c * uv[2, 1]
    texture_x = np.floor(u).astype(int) % texture.shape[1]
    texture_y = np.floor(v).astype(int) % texture.shape[0]
    sampled = texture[texture_y, texture_x].copy()
    opaque = sampled[:, :, 3] > 0
    visible &= opaque
    if not visible.any():
        return

    sampled[:, :, :3] = np.clip(sampled[:, :, :3].astype(float) * shade, 0, 255).astype(np.uint8)
    region_output = output[min_y : max_y + 1, min_x : max_x + 1]
    region_output[visible] = sampled[visible]
    region_depth[visible] = depth[visible]


def render_pet(model: dict, texture_image: Image.Image) -> Image.Image:
    faces = scene_transform(prepare_faces(model))
    all_vertices = np.concatenate([vertices for vertices, _ in faces], axis=0)
    minimum = all_vertices.min(axis=0)
    maximum = all_vertices.max(axis=0)
    center = (minimum[:2] + maximum[:2]) / 2
    width, height = maximum[0] - minimum[0], maximum[1] - minimum[1]
    scale = min(
        (RENDER_SIZE - 2 * RENDER_MARGIN) / width,
        (RENDER_SIZE - 2 * RENDER_MARGIN) / height,
    )

    texture = np.asarray(texture_image.convert("RGBA"))
    output = np.zeros((RENDER_SIZE, RENDER_SIZE, 4), dtype=np.uint8)
    depth_buffer = np.full((RENDER_SIZE, RENDER_SIZE), np.inf, dtype=float)
    light = np.asarray([-0.35, 0.5, -1.0], dtype=float)
    light /= np.linalg.norm(light)

    for vertices, uv in faces:
        normal = np.cross(vertices[1] - vertices[0], vertices[3] - vertices[0])
        normal_length = np.linalg.norm(normal)
        if normal_length < 1e-9:
            continue
        normal /= normal_length
        if normal[2] >= 0.05:
            continue

        shade = 0.78 + 0.22 * max(0.0, float(np.dot(normal, -light)))
        screen = np.column_stack(
            (
                (vertices[:, 0] - center[0]) * scale + RENDER_SIZE / 2,
                RENDER_SIZE / 2 - (vertices[:, 1] - center[1]) * scale,
                vertices[:, 2],
            )
        )

        for indices in ((0, 1, 2), (0, 2, 3)):
            rasterize_triangle(
                output,
                depth_buffer,
                texture,
                screen[list(indices)],
                uv[list(indices)],
                shade,
            )

    image = Image.fromarray(output, "RGBA")
    bounds = image.getbbox()
    if not bounds:
        return Image.new("RGBA", (CELL_SIZE, CELL_SIZE), (0, 0, 0, 0))

    cropped = image.crop(bounds)
    square_size = max(cropped.size)
    square = Image.new("RGBA", (square_size, square_size), (0, 0, 0, 0))
    square.alpha_composite(
        cropped,
        ((square_size - cropped.width) // 2, (square_size - cropped.height) // 2),
    )
    return square.resize((CELL_SIZE, CELL_SIZE), Image.Resampling.NEAREST)


def render_pet_file(model_path: str, skin_path: str, output_path: str) -> str:
    model = json.loads(Path(model_path).read_text(encoding="utf-8"))
    texture = Image.open(skin_path)
    rendered = render_pet(model, texture)
    rendered.save(output_path, format="PNG", optimize=True)
    return output_path


def render_all(item_ids: list[str], skins_dir: Path, cells_dir: Path) -> None:
    worker_count = min(4, max(1, os.cpu_count() or 1))
    cells_dir.mkdir(parents=True, exist_ok=True)

    with concurrent.futures.ProcessPoolExecutor(max_workers=worker_count) as executor:
        futures = [
            executor.submit(
                render_pet_file,
                str(MODEL_PATH),
                str(skins_dir / f"{item_id}.png"),
                str(cells_dir / f"{item_id}.png"),
            )
            for item_id in item_ids
        ]
        for future in concurrent.futures.as_completed(futures):
            future.result()


def build_atlas(item_ids: list[str], cells_dir: Path) -> tuple[int, int]:
    rows = math.ceil(len(item_ids) / ATLAS_COLUMNS)
    atlas = Image.new(
        "RGBA",
        (ATLAS_COLUMNS * CELL_SIZE, rows * CELL_SIZE),
        (0, 0, 0, 0),
    )

    for index, item_id in enumerate(item_ids):
        cell = Image.open(cells_dir / f"{item_id}.png").convert("RGBA")
        x = (index % ATLAS_COLUMNS) * CELL_SIZE
        y = (index // ATLAS_COLUMNS) * CELL_SIZE
        atlas.alpha_composite(cell, (x, y))

    ATLAS_PATH.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(ATLAS_PATH, format="WEBP", lossless=True, method=6)
    return ATLAS_COLUMNS, rows


def write_typescript(item_ids: list[str], columns: int, rows: int) -> None:
    mappings = "\n".join(f'  "{item_id}": {index},' for index, item_id in enumerate(item_ids))
    source = f'''import type {{ SaoItemAtlas, SaoItemAtlasSprite }} from "./saoTrackedItemArtwork"

export const SAO_PET_ATLAS_PATH = "/event-icons/sao-pets.webp"
export const SAO_PET_ATLAS_COLUMNS = {columns}
export const SAO_PET_ATLAS_ROWS = {rows}
export const SAO_PET_ATLAS_ITEM_COUNT = {len(item_ids)}

const PET_ATLAS: SaoItemAtlas = {{
  path: SAO_PET_ATLAS_PATH,
  columns: SAO_PET_ATLAS_COLUMNS,
  rows: SAO_PET_ATLAS_ROWS,
}}

const PET_ATLAS_INDEX: Record<string, number> = {{
{mappings}
}}

export function resolveSaoPetAtlasSprite(localizationKey?: string): SaoItemAtlasSprite | null {{
  if (!localizationKey?.startsWith("sao.item.pet_") || !localizationKey.endsWith(".name")) {{
    return null
  }}

  const itemId = localizationKey.slice("sao.item.".length, -".name".length)
  const index = PET_ATLAS_INDEX[itemId]

  if (index === undefined) {{
    return null
  }}

  return {{
    column: index % PET_ATLAS.columns,
    row: Math.floor(index / PET_ATLAS.columns),
    atlas: PET_ATLAS,
  }}
}}
'''
    TYPESCRIPT_PATH.write_text(source, encoding="utf-8")


def main() -> None:
    if not MODEL_PATH.exists():
        raise SystemExit(f"Missing pet model: {MODEL_PATH}")

    item_ids = discover_pet_ids()
    if not item_ids:
        raise SystemExit("No SAO pet items discovered from localization")

    with tempfile.TemporaryDirectory(prefix="mnemos-sao-pets-") as temp:
        temp_dir = Path(temp)
        skins_dir = temp_dir / "skins"
        cells_dir = temp_dir / "cells"
        skins_dir.mkdir()

        print(f"Discovered {len(item_ids)} SAO pet items")
        download_skins(item_ids, skins_dir)
        print("Downloaded all SAO pet skins")
        render_all(item_ids, skins_dir, cells_dir)
        print("Rendered all SAO pet models")
        columns, rows = build_atlas(item_ids, cells_dir)
        write_typescript(item_ids, columns, rows)

    print(f"Wrote {ATLAS_PATH.relative_to(ROOT)}")
    print(f"Wrote {TYPESCRIPT_PATH.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
