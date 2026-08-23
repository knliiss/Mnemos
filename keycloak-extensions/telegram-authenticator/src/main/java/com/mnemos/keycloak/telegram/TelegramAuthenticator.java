package com.mnemos.keycloak.telegram;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public final class TelegramAuthenticator implements Authenticator {
    private static final Logger LOGGER = Logger.getLogger(TelegramAuthenticator.class);

    private static final String INIT_DATA_PARAMETER = "telegram_init_data";
    private static final String TELEGRAM_LOGIN_TEMPLATE = "telegram-login.ftl";
    private static final String TELEGRAM_USER_ID_ATTRIBUTE = "telegram_user_id";
    private static final String TELEGRAM_USERNAME_ATTRIBUTE = "telegram_username";
    private static final String TELEGRAM_LANGUAGE_ATTRIBUTE = "telegram_language";
    private static final String TELEGRAM_PREMIUM_ATTRIBUTE = "telegram_premium";
    private static final String TELEGRAM_PHOTO_URL_ATTRIBUTE = "telegram_photo_url";
    private static final String DEFAULT_USERS_GROUP_PATH = "/users";
    private static final String SUPER_ADMINS_GROUP_PATH = "/super-admins";

    private final TelegramInitDataValidator validator;
    private final Long superAdminTelegramId;

    public TelegramAuthenticator(
            TelegramInitDataValidator validator,
            Long superAdminTelegramId
    ) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.superAdminTelegramId = superAdminTelegramId;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        try {
            String initData = context.getHttpRequest().getUri().getQueryParameters().getFirst(INIT_DATA_PARAMETER);
            LOGGER.debugf(
                    "Telegram authentication started: realm=%s, clientId=%s, hasInitData=%s, initDataLength=%d",
                    context.getRealm().getName(),
                    context.getAuthenticationSession().getClient().getClientId(),
                    initData != null && !initData.isBlank(),
                    initData != null ? initData.length() : 0
            );

            if (initData == null || initData.isBlank()) {
                LOGGER.debug("Telegram authentication requires a challenge because initData is not present");
                context.challenge(context.form().createForm(TELEGRAM_LOGIN_TEMPLATE));
                return;
            }

            authenticateTelegramUser(context, initData);
        } catch (Exception exception) {
            LOGGER.error("Unexpected error while reading Telegram authentication request", exception);
            fail(
                    context,
                    AuthenticationFlowError.INTERNAL_ERROR,
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Telegram authentication failed"
            );
        }
    }

    private void authenticateTelegramUser(AuthenticationFlowContext context, String rawInitData) {
        try {
            Optional<TelegramInitData> validationResult = validator.validate(rawInitData);

            if (validationResult.isEmpty()) {
                LOGGER.warnf(
                        "Telegram initData rejected by validator: realm=%s, clientId=%s, initDataLength=%d",
                        context.getRealm().getName(),
                        context.getAuthenticationSession().getClient().getClientId(),
                        rawInitData.length()
                );
                fail(
                        context,
                        AuthenticationFlowError.INVALID_CREDENTIALS,
                        Response.Status.UNAUTHORIZED,
                        "Invalid Telegram authentication data"
                );
                return;
            }

            TelegramInitData initData = validationResult.get();
            TelegramUser telegramUser = Objects.requireNonNull(
                    initData.user(),
                    "Telegram user is missing from initData"
            );
            UserModel user = findOrCreateUser(context, telegramUser);

            updateUser(user, telegramUser);
            joinDefaultUsersGroup(context, user);
            synchronizeSuperAdminGroup(context, user, telegramUser.id());

            context.setUser(user);
            context.success();

            LOGGER.infof(
                    "Telegram user authenticated: telegramUserId=%s, keycloakUserId=%s",
                    telegramUser.id(),
                    user.getId()
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            LOGGER.warnf(exception, "Telegram authentication data is invalid");
            fail(
                    context,
                    AuthenticationFlowError.INVALID_CREDENTIALS,
                    Response.Status.UNAUTHORIZED,
                    "Invalid Telegram authentication data"
            );
        } catch (Exception exception) {
            LOGGER.error("Unexpected Telegram authentication error", exception);
            fail(
                    context,
                    AuthenticationFlowError.INTERNAL_ERROR,
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Telegram authentication failed"
            );
        }
    }

    private UserModel findOrCreateUser(AuthenticationFlowContext context, TelegramUser telegramUser) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        String telegramUserId = Long.toString(telegramUser.id());

        UserModel existingUser = session.users()
                .searchForUserByUserAttributeStream(realm, TELEGRAM_USER_ID_ATTRIBUTE, telegramUserId)
                .findFirst()
                .orElse(null);

        if (existingUser != null) {
            if (!existingUser.isEnabled()) {
                throw new IllegalArgumentException("Telegram user is disabled");
            }

            return existingUser;
        }

        String username = resolveUsername(telegramUser);
        UserModel user = session.users().addUser(realm, username);

        user.setEnabled(true);
        user.setSingleAttribute(TELEGRAM_USER_ID_ATTRIBUTE, telegramUserId);

        LOGGER.infof(
                "Created Keycloak user for Telegram identity: telegramUserId=%s, keycloakUserId=%s",
                telegramUserId,
                user.getId()
        );

        return user;
    }

    private void updateUser(UserModel user, TelegramUser telegramUser) {
        setOptionalRootAttribute(telegramUser.firstName(), user::setFirstName);
        setOptionalRootAttribute(telegramUser.lastName(), user::setLastName);
        setOptionalAttribute(user, TELEGRAM_USERNAME_ATTRIBUTE, telegramUser.username());
        setOptionalAttribute(user, TELEGRAM_LANGUAGE_ATTRIBUTE, telegramUser.languageCode());
        setOptionalAttribute(user, TELEGRAM_PHOTO_URL_ATTRIBUTE, telegramUser.photoUrl());
        user.setSingleAttribute(
                TELEGRAM_PREMIUM_ATTRIBUTE,
                Boolean.toString(Boolean.TRUE.equals(telegramUser.premium()))
        );
    }

    private void joinDefaultUsersGroup(AuthenticationFlowContext context, UserModel user) {
        GroupModel usersGroup = KeycloakModelUtils.findGroupByPath(
                context.getSession(),
                context.getRealm(),
                DEFAULT_USERS_GROUP_PATH
        );

        if (usersGroup == null) {
            LOGGER.warnf("Default users group was not found: path=%s", DEFAULT_USERS_GROUP_PATH);
            return;
        }

        if (!isMember(user, usersGroup)) {
            user.joinGroup(usersGroup);
        }
    }

    private void synchronizeSuperAdminGroup(
            AuthenticationFlowContext context,
            UserModel user,
            long telegramUserId
    ) {
        if (superAdminTelegramId == null) {
            return;
        }

        GroupModel superAdminsGroup = KeycloakModelUtils.findGroupByPath(
                context.getSession(),
                context.getRealm(),
                SUPER_ADMINS_GROUP_PATH
        );

        if (superAdminsGroup == null) {
            throw new IllegalStateException("Super admin group was not found: " + SUPER_ADMINS_GROUP_PATH);
        }

        boolean shouldBeSuperAdmin = superAdminTelegramId == telegramUserId;
        boolean isSuperAdmin = isMember(user, superAdminsGroup);

        if (shouldBeSuperAdmin && !isSuperAdmin) {
            user.joinGroup(superAdminsGroup);
        } else if (!shouldBeSuperAdmin && isSuperAdmin) {
            user.leaveGroup(superAdminsGroup);
        }
    }

    private boolean isMember(UserModel user, GroupModel group) {
        return user.getGroupsStream().anyMatch(candidate -> group.getId().equals(candidate.getId()));
    }

    private void setOptionalAttribute(UserModel user, String attributeName, String value) {
        if (value == null || value.isBlank()) {
            user.removeAttribute(attributeName);
            return;
        }

        user.setSingleAttribute(attributeName, value);
    }

    private void setOptionalRootAttribute(String value, Consumer<String> setter) {
        if (value == null || value.isBlank()) {
            return;
        }

        setter.accept(value);
    }

    private void fail(
            AuthenticationFlowContext context,
            AuthenticationFlowError error,
            Response.Status status,
            String message
    ) {
        context.failure(
                error,
                Response.status(status)
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .entity(message)
                        .build()
        );
    }

    private String resolveUsername(TelegramUser telegramUser) {
        String telegramUsername = telegramUser.username();

        if (telegramUsername != null && !telegramUsername.isBlank()) {
            return telegramUsername;
        }

        String baseName = firstNonBlank(
                telegramUser.firstName(),
                telegramUser.lastName(),
                "unknown"
        );

        return normalizeUsername(baseName) + "_" + telegramUser.id();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "unknown";
    }

    private String normalizeUsername(String value) {
        String normalized = value.trim()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9._-]", "");

        return normalized.isBlank() ? "unknown" : normalized;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String initData = context.getHttpRequest()
                .getDecodedFormParameters()
                .getFirst(INIT_DATA_PARAMETER);

        if (initData == null || initData.isBlank()) {
            fail(
                    context,
                    AuthenticationFlowError.INVALID_CREDENTIALS,
                    Response.Status.BAD_REQUEST,
                    "Telegram authentication data is missing"
            );
            return;
        }

        authenticateTelegramUser(context, initData);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
