import { useState } from "react"

const skinUrl = import.meta.env.VITE_SKIN_URL ?? "https://webdata.c7x.dev/textures/skin/46d7a80e-74f5-11eb-acca-1cb72caa35fd"

export function MinecraftHead({ size = 48, className = "" }: { size?: number; className?: string }) {
  const [failed, setFailed] = useState(false)
  return (
    <div className={`minecraft-head ${className}`} style={{ width: size, height: size }}>
      {!failed ? <img src={skinUrl} alt="" onError={() => setFailed(true)} /> : <div className="pixel-fallback"><span /></div>}
    </div>
  )
}
