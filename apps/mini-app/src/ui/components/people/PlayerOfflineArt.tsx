type Props = {
  size?: number
}

export function PlayerOfflineArt({ size = 38 }: Props) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      role="img"
      aria-label="Player offline"
    >
      <path d="M9 13.5 24 5l15 8.5v18L24 40 9 31.5z" fill="#151b12" stroke="#48513f" />
      <path d="M16 18h16v13H16z" fill="#20291a" stroke="#73805f" />
      <path d="M19 21h4v4h-4zm6 0h4v4h-4z" fill="#cbff2d" opacity=".8" />
      <path d="M20 29h8" stroke="#889378" strokeWidth="2" strokeLinecap="round" />
      <path d="m34 11 5-2-2 5 4 1-5 2" fill="none" stroke="#b98dc2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="36" cy="30" r="5" fill="#0b0e0a" stroke="#59634e" />
      <path d="M36 27v3l2 1" fill="none" stroke="#cbff2d" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  )
}
