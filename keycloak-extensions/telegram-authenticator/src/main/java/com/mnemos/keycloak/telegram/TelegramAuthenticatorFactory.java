package com.mnemos.keycloak.telegram;

import java.time.Duration;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public final class TelegramAuthenticatorFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "mnemos-telegram-mini-app";

    private static final Logger LOG = Logger.getLogger(TelegramAuthenticatorFactory.class);
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    private volatile TelegramAuthenticator authenticator;

    @Override
    public Authenticator create(KeycloakSession session) {
        TelegramAuthenticator current = authenticator;

        if (current == null) {
            throw new IllegalStateException("Telegram authenticator was not initialized");
        }

        return current;
    }

    @Override
    public void init(Config.Scope config) {
        String botToken = readRequiredEnvironment("MNEMOS_TELEGRAM_BOT_TOKEN");
        long maximumAgeSeconds = readPositiveLongEnvironment(
                "MNEMOS_TELEGRAM_INIT_DATA_MAX_AGE_SECONDS",
                300
        );
        Long superAdminTelegramId = readOptionalLongEnvironment("MNEMOS_SUPER_ADMIN_TELEGRAM_ID");

        TelegramInitDataValidator validator = new TelegramInitDataValidator(
                botToken,
                Duration.ofSeconds(maximumAgeSeconds)
        );

        authenticator = new TelegramAuthenticator(
                validator,
                superAdminTelegramId
        );

        LOG.infof(
                "Initialized Mnemos Telegram authenticator. Maximum initData age: %d seconds",
                maximumAgeSeconds
        );
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return "telegram";
    }

    @Override
    public String getDisplayType() {
        return "Mnemos Telegram Mini App";
    }

    @Override
    public String getHelpText() {
        return "Authenticates a user using validated Telegram Mini App initData.";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    private static String readRequiredEnvironment(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }

        return value;
    }

    private static Long readOptionalLongEnvironment(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    name + " must contain a valid integer",
                    exception
            );
        }
    }

    private static long readPositiveLongEnvironment(
            String name,
            long defaultValue
    ) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            long parsed = Long.parseLong(value);

            if (parsed <= 0) {
                throw new IllegalArgumentException(name + " must be positive");
            }

            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    name + " must contain a valid integer",
                    exception
            );
        }
    }
}
