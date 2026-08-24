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

        <script>
            (() => {
                const form =
                    document.getElementById("kc-telegram-form");

                const initDataInput =
                    document.getElementById("telegram_init_data");

                const statusElement =
                    document.getElementById("telegram-status");

                const fragment =
                    window.location.hash.startsWith("#")
                        ? window.location.hash.slice(1)
                        : window.location.hash;

                const fragmentParameters =
                    new URLSearchParams(fragment);

                const initData =
                    fragmentParameters.get("telegram_init_data");

                if (initData) {
                    window.history.replaceState(
                        null,
                        document.title,
                        `${window.location.pathname}${window.location.search}`
                    );
                }

                if (!initData) {
                    statusElement.textContent =
                        "Telegram authentication data is unavailable. Return to Mnemos and try again.";
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
