import { useEffect, useState } from "react"
import { apiBlob } from "@/shared/api/httpClient"

type Props = {
  src: string
  alt?: string
  className?: string
  draggable?: boolean
}

export function PersonalizationAssetImage({
  src,
  alt = "",
  className,
  draggable = false,
}: Props) {
  const [objectUrl, setObjectUrl] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    let nextObjectUrl: string | null = null

    void apiBlob(src)
      .then((blob) => {
        if (cancelled) {
          return
        }

        nextObjectUrl = URL.createObjectURL(blob)
        setObjectUrl(nextObjectUrl)
      })
      .catch(() => {
        if (!cancelled) {
          setObjectUrl(null)
        }
      })

    return () => {
      cancelled = true

      if (nextObjectUrl) {
        URL.revokeObjectURL(nextObjectUrl)
      }
    }
  }, [src])

  if (!objectUrl) {
    return null
  }

  return <img src={objectUrl} alt={alt} className={className} draggable={draggable} />
}
