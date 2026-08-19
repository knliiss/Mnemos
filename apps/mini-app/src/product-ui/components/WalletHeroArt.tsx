export function WalletHeroArt() {
  return (
    <svg className="wallet-hero-art" viewBox="0 0 220 150" fill="none" aria-hidden="true">
      <defs>
        <linearGradient id="wallet-ring" x1="38" y1="28" x2="170" y2="126" gradientUnits="userSpaceOnUse">
          <stop stopColor="#F6C467" />
          <stop offset="0.58" stopColor="#D9FF37" />
          <stop offset="1" stopColor="#6F7D2D" />
        </linearGradient>
        <radialGradient id="wallet-glow" cx="0" cy="0" r="1" gradientTransform="translate(124 84) rotate(90) scale(62 92)" gradientUnits="userSpaceOnUse">
          <stop stopColor="#D9FF37" stopOpacity="0.2" />
          <stop offset="1" stopColor="#D9FF37" stopOpacity="0" />
        </radialGradient>
      </defs>

      <ellipse cx="124" cy="84" rx="92" ry="62" fill="url(#wallet-glow)" />
      <ellipse cx="118" cy="92" rx="68" ry="23" stroke="#D9FF37" strokeOpacity="0.13" />
      <ellipse cx="118" cy="92" rx="49" ry="15" stroke="#F6C467" strokeOpacity="0.14" />

      <g transform="translate(74 27) rotate(-14 49 49)">
        <circle cx="49" cy="49" r="38" fill="#15160F" stroke="url(#wallet-ring)" strokeWidth="2" />
        <circle cx="49" cy="49" r="30" fill="#0B0C08" stroke="#F4E6BD" strokeOpacity="0.14" />
        <path d="M61 37c-3.2-3.2-7.2-4.8-12-4.8-9.7 0-16.5 7-16.5 16.8S39.3 65.8 49 65.8c4.8 0 8.8-1.6 12-4.8" stroke="#E6FF69" strokeWidth="4" strokeLinecap="round" />
      </g>

      <circle cx="46" cy="41" r="2.2" fill="#F6C467" fillOpacity="0.8" />
      <circle cx="176" cy="34" r="1.8" fill="#D9FF37" fillOpacity="0.8" />
      <circle cx="181" cy="118" r="1.4" fill="#D9FF37" fillOpacity="0.55" />
    </svg>
  )
}
