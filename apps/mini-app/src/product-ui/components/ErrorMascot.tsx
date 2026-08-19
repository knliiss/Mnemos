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
  validation: "#D6A653",
  access: "#C98BA9",
  conflict: "#D98B55",
  "not-found": "#76C8CE",
  waiting: "#B9CF68",
  internal: "#A98AB2",
}

export function ErrorMascot({ variant }: Props) {
  const tone = tones[variant]
  const lookingAside = variant === "not-found" || variant === "internal"
  const pupilShift = lookingAside ? 2 : 0

  return (
    <svg
      className={`error-mascot error-mascot--${variant}`}
      viewBox="0 0 84 84"
      fill="none"
      preserveAspectRatio="xMidYMid meet"
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      <g className="error-mascot__cat">
        <rect x="27" y="62" width="30" height="4" fill="#111411" />
        <rect x="23" y="66" width="38" height="8" fill="#111411" />
        <rect x="27" y="66" width="30" height="6" fill="#373D37" />

        <rect x="22" y="24" width="8" height="14" fill="#111411" />
        <rect x="26" y="20" width="8" height="14" fill="#111411" />
        <rect x="50" y="20" width="8" height="14" fill="#111411" />
        <rect x="54" y="24" width="8" height="14" fill="#111411" />
        <rect x="26" y="24" width="4" height="7" fill="#A97980" />
        <rect x="54" y="24" width="4" height="7" fill="#A97980" />

        <rect x="20" y="34" width="44" height="26" fill="#111411" />
        <rect x="24" y="34" width="36" height="22" fill="#383E38" />
        <rect x="28" y="38" width="28" height="4" fill="#444B44" />

        <rect x="26" y="43" width="11" height="9" fill="#171A17" />
        <rect x="47" y="43" width="11" height="9" fill="#171A17" />
        <rect x="29" y="43" width="6" height="6" fill="#B8CA79" />
        <rect x="50" y="43" width="6" height="6" fill="#B8CA79" />
        <rect x={31 + pupilShift} y="45" width="3" height="5" fill="#090B09" />
        <rect x={52 + pupilShift} y="45" width="3" height="5" fill="#090B09" />

        <rect x="32" y="52" width="20" height="4" fill="#E5DED3" />
        <rect x="36" y="56" width="12" height="4" fill="#E5DED3" />
        <rect x="39" y="51" width="6" height="4" fill="#B98689" />

        <rect x="32" y="60" width="20" height="3" fill="#BB914F" />
        <rect x="40" y="62" width="4" height="5" fill="#111411" />
        <rect x="41" y="63" width="2" height="2" fill={tone} />
      </g>

      <ErrorAccessory variant={variant} tone={tone} />
    </svg>
  )
}

function ErrorAccessory({ variant, tone }: { variant: ErrorMascotVariant; tone: string }) {
  switch (variant) {
    case "validation":
      return (
        <g transform="translate(60 48)">
          <rect width="16" height="21" fill="#141814" />
          <rect x="3" y="3" width="10" height="15" fill="#333A33" />
          <rect x="7" y="6" width="3" height="7" fill={tone} />
          <rect x="7" y="15" width="3" height="3" fill={tone} />
        </g>
      )

    case "access":
      return (
        <g transform="translate(60 48)">
          <rect x="2" y="8" width="16" height="14" fill="#151915" />
          <rect x="5" y="11" width="10" height="8" fill={tone} />
          <rect x="6" y="3" width="8" height="8" fill="#151915" />
          <rect x="8" y="5" width="4" height="5" fill="#343A34" />
          <rect x="9" y="14" width="2" height="4" fill="#20251F" />
        </g>
      )

    case "conflict":
      return (
        <g transform="translate(59 49)">
          <path d="M0 5h13V1l7 6-7 6V9H0V5Z" fill={tone} />
          <path d="M20 18H7v4l-7-6 7-6v4h13v4Z" fill="#788274" />
        </g>
      )

    case "not-found":
      return (
        <g transform="translate(59 48)">
          <rect x="1" y="1" width="13" height="13" fill="#141814" />
          <rect x="4" y="4" width="7" height="7" fill="#2C3530" />
          <rect x="13" y="12" width="4" height="10" fill={tone} />
          <rect x="16" y="19" width="7" height="4" fill={tone} />
        </g>
      )

    case "waiting":
      return (
        <g transform="translate(61 48)">
          <rect x="1" width="16" height="3" fill="#151915" />
          <rect x="1" y="19" width="16" height="3" fill="#151915" />
          <rect x="4" y="3" width="10" height="4" fill={tone} />
          <rect x="7" y="7" width="4" height="8" fill="#A88A51" />
          <rect x="4" y="15" width="10" height="4" fill={tone} />
        </g>
      )

    case "internal":
      return (
        <g transform="translate(60 48)">
          <rect width="18" height="21" fill="#141814" />
          <rect x="3" y="3" width="12" height="15" fill="#2C322C" />
          <rect x="7" y="5" width="5" height="3" fill={tone} />
          <rect x="10" y="8" width="3" height="4" fill={tone} />
          <rect x="7" y="15" width="3" height="3" fill={tone} />
        </g>
      )
  }
}
