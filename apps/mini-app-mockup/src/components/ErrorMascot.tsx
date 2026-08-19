export type ErrorMascotVariant =
  | "validation"
  | "access"
  | "conflict"
  | "not-found"
  | "waiting"
  | "internal"

type Props = {
  variant: ErrorMascotVariant
}

const tones: Record<ErrorMascotVariant, string> = {
  validation: "#d8aa52",
  access: "#c98ba9",
  conflict: "#d98b55",
  "not-found": "#76c8ce",
  waiting: "#b9cf68",
  internal: "#a98ab2",
}

export function ErrorMascot({ variant }: Props) {
  const tone = tones[variant]
  const lookingAside = variant === "internal" || variant === "not-found"
  const pupilShift = lookingAside ? 2 : 0

  return (
    <svg
      className={`error-mascot error-mascot--${variant}`}
      viewBox="0 0 104 76"
      fill="none"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      <AccessoryBack variant={variant} tone={tone} />

      <g className="error-mascot__cat">
        <rect x="35" y="54" width="38" height="4" fill="#111411" />
        <rect x="31" y="58" width="46" height="12" fill="#111411" />
        <rect x="35" y="58" width="38" height="8" fill="#343934" />
        <rect x="42" y="62" width="24" height="8" fill="#3f453f" />

        <rect x="26" y="18" width="8" height="16" fill="#111411" />
        <rect x="30" y="14" width="8" height="16" fill="#111411" />
        <rect x="70" y="14" width="8" height="16" fill="#111411" />
        <rect x="74" y="18" width="8" height="16" fill="#111411" />
        <rect x="30" y="18" width="4" height="8" fill="#a97980" />
        <rect x="74" y="18" width="4" height="8" fill="#a97980" />

        <rect x="28" y="28" width="52" height="4" fill="#111411" />
        <rect x="24" y="32" width="60" height="24" fill="#111411" />
        <rect x="28" y="32" width="52" height="20" fill="#363b36" />
        <rect x="32" y="52" width="44" height="4" fill="#363b36" />
        <rect x="42" y="32" width="24" height="4" fill="#444a44" />

        <rect x="32" y="38" width="16" height="12" fill="#171a17" />
        <rect x="60" y="38" width="16" height="12" fill="#171a17" />
        <rect x="36" y="38" width="8" height="8" fill="#b8ca79" />
        <rect x="64" y="38" width="8" height="8" fill="#b8ca79" />
        <rect x={38 + pupilShift} y="40" width="4" height="6" fill="#090b09" />
        <rect x={66 + pupilShift} y="40" width="4" height="6" fill="#090b09" />
        <rect x="36" y="38" width="2" height="2" fill="#f1f4e9" />
        <rect x="64" y="38" width="2" height="2" fill="#f1f4e9" />

        <rect x="44" y="46" width="20" height="4" fill="#e5ded3" />
        <rect x="40" y="50" width="28" height="6" fill="#e5ded3" />
        <rect x="50" y="46" width="8" height="4" fill="#b98689" />
        <rect x="52" y="50" width="4" height="2" fill="#765d60" />
        <rect x="47" y="53" width="4" height="2" fill="#656962" />
        <rect x="57" y="53" width="4" height="2" fill="#656962" />

        <rect x="40" y="56" width="28" height="3" fill="#bb914f" />
        <rect x="51" y="58" width="6" height="6" fill="#111411" />
        <rect x="53" y="60" width="2" height="2" fill={tone} />

        <rect x="20" y="45" width="12" height="2" fill="#767c75" opacity=".62" />
        <rect x="76" y="45" width="12" height="2" fill="#767c75" opacity=".62" />
      </g>

      <AccessoryFront variant={variant} tone={tone} />
    </svg>
  )
}

function AccessoryBack({ variant, tone }: { variant: ErrorMascotVariant; tone: string }) {
  if (variant === "conflict") {
    return (
      <g opacity=".86">
        <rect x="12" y="22" width="22" height="4" fill={tone} />
        <rect x="12" y="18" width="4" height="12" fill={tone} />
        <rect x="8" y="18" width="8" height="4" fill={tone} />
        <rect x="70" y="16" width="22" height="4" fill="#7e8f72" />
        <rect x="88" y="12" width="4" height="12" fill="#7e8f72" />
        <rect x="88" y="12" width="8" height="4" fill="#7e8f72" />
      </g>
    )
  }

  if (variant === "waiting") {
    return (
      <g>
        <rect x="10" y="19" width="14" height="3" fill={tone} />
        <rect x="10" y="37" width="14" height="3" fill={tone} />
        <rect x="13" y="22" width="8" height="3" fill="#d8d1bd" />
        <rect x="15" y="25" width="4" height="9" fill="#a6884d" />
        <rect x="13" y="34" width="8" height="3" fill="#d8d1bd" />
      </g>
    )
  }

  if (variant === "not-found") {
    return (
      <g opacity=".8">
        <rect x="84" y="15" width="4" height="4" fill={tone} />
        <rect x="92" y="23" width="3" height="3" fill="#d7a65a" />
      </g>
    )
  }

  if (variant === "internal") {
    return (
      <g opacity=".86">
        <rect x="14" y="18" width="4" height="4" fill={tone} />
        <rect x="9" y="26" width="3" height="3" fill="#d59f4e" />
        <rect x="88" y="17" width="3" height="3" fill="#c7f24a" />
        <path d="M88 31h4v4h-4v4h-4v-4h-4v-4h4v-4h4v4Z" fill={tone} />
      </g>
    )
  }

  return <rect x="88" y="21" width="3" height="3" fill={tone} opacity=".72" />
}

function AccessoryFront({ variant, tone }: { variant: ErrorMascotVariant; tone: string }) {
  if (variant === "validation") {
    return (
      <g transform="translate(71 44)">
        <rect width="21" height="25" fill="#151915" />
        <rect x="3" y="3" width="15" height="19" fill="#30362f" />
        <rect x="7" y="7" width="7" height="3" fill={tone} />
        <rect x="9" y="12" width="3" height="6" fill={tone} />
      </g>
    )
  }

  if (variant === "access") {
    return (
      <g transform="translate(70 43)">
        <path d="M11 0 22 4v9c0 8-5 12-11 15C5 25 0 21 0 13V4L11 0Z" fill="#151915" />
        <path d="M11 4 18 7v6c0 5-3 8-7 10-4-2-7-5-7-10V7l7-3Z" fill={tone} opacity=".78" />
        <rect x="8" y="11" width="6" height="7" fill="#20251f" />
        <rect x="9" y="8" width="4" height="5" fill="none" stroke="#20251f" strokeWidth="2" />
      </g>
    )
  }

  if (variant === "not-found") {
    return (
      <g transform="translate(71 42)">
        <rect x="2" y="2" width="14" height="14" fill="#121512" />
        <rect x="5" y="5" width="8" height="8" fill="#26302b" />
        <rect x="16" y="15" width="4" height="12" fill={tone} />
        <rect x="19" y="24" width="8" height="4" fill={tone} />
      </g>
    )
  }

  if (variant === "waiting") {
    return (
      <g transform="translate(77 48)">
        <rect x="0" y="8" width="18" height="3" fill="#151915" />
        <rect x="5" y="2" width="8" height="3" fill={tone} opacity=".75" />
        <rect x="8" y="0" width="3" height="3" fill={tone} />
      </g>
    )
  }

  if (variant === "internal") {
    return (
      <g transform="translate(73 46)">
        <rect x="0" y="0" width="18" height="20" fill="#151915" />
        <rect x="3" y="3" width="12" height="14" fill="#292f29" />
        <rect x="6" y="5" width="6" height="3" fill={tone} />
        <rect x="9" y="8" width="3" height="4" fill={tone} />
        <rect x="6" y="14" width="3" height="3" fill={tone} />
      </g>
    )
  }

  return null
}
