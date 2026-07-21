<#import "template.ftl" as layout>

<@layout.registrationLayout
displayMessage=true
displayInfo=false;
section
>
    <#if section = "header">
        Telegram authentication

    <#elseif section = "form">
        <form
                id="kc-telegram-form"
                action="${url.loginAction}"
                method="post"
        >
            <input
                    id="telegram_init_data"
                    name="telegram_init_data"
                    type="hidden"
            >

            <div id="telegram-status">
                Connecting to Telegram…
            </div>

            <noscript>
                JavaScript is required for Telegram authentication.
            </noscript>
        </form>

        <script src="https://telegram.org/js/telegram-web-app.js"></script>

        <script>
            (() => {
                const form =
                    document.getElementById("kc-telegram-form");

                const initDataInput =
                    document.getElementById("telegram_init_data");

                const statusElement =
                    document.getElementById("telegram-status");

                const telegram =
                    window.Telegram?.WebApp;

                if (!telegram) {
                    statusElement.textContent =
                        "Telegram WebApp API is unavailable. Open Mnemos through Telegram.";
                    return;
                }

                telegram.ready();
                telegram.expand();

                const initData = telegram.initData;

                if (!initData) {
                    statusElement.textContent =
                        "Telegram initData is empty. Open Mnemos through the bot.";
                    return;
                }

                initDataInput.value = initData;

                statusElement.textContent =
                    "Telegram data received. Signing in…";

                form.requestSubmit();
            })();
        </script>
    </#if>
</@layout.registrationLayout>