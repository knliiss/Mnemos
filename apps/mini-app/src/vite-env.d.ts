/// <reference types="vite/client" />
interface ImportMetaEnv {
    readonly VITE_KEYCLOAK_URL: string;
    readonly VITE_KEYCLOAK_REALM: string;
    readonly VITE_KEYCLOAK_CLIENT_ID: string;
    readonly VITE_API_URL: string;
    readonly VITE_SKIN_URL: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}

interface Window {
    Telegram?: { WebApp: TelegramWebApp }
}

interface TelegramWebApp {
    initData: string;

    ready(): void;

    expand(): void;

    setHeaderColor(color: string): void;

    setBackgroundColor(color: string): void;

    colorScheme?: "light" | "dark";
    BackButton?: { show(): void; hide(): void; onClick(cb: () => void): void; offClick(cb: () => void): void };
    HapticFeedback?: { impactOccurred(style: "light" | "medium" | "heavy"): void }
}