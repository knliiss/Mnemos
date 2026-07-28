export const telegram = window.Telegram?.WebApp

export function initializeTelegram() {
    if (!telegram) return;
    telegram.ready();
    telegram.expand();
    telegram.setHeaderColor("#07090c");
    telegram.setBackgroundColor("#07090c")
}

export function haptic() {
    telegram?.HapticFeedback?.impactOccurred("light")
}