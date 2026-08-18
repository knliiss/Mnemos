_mnemos_make_project_root() {
    local dir="$PWD"

    while [[ "$dir" != "/" ]]; do
        if [[ -f "$dir/Makefile" ]] && grep -q '^MNEMOS_PROJECT[[:space:]]*:=[[:space:]]*1' "$dir/Makefile" 2>/dev/null; then
            REPLY="$dir"
            return 0
        fi
        dir="${dir:h}"
    done

    return 1
}

_mnemos_make() {
    local root
    local -a targets
    local service

    if ! _mnemos_make_project_root; then
        autoload -Uz _make
        _make "$@"
        return
    fi

    root="$REPLY"
    targets=(
        'help:Show available Mnemos commands'
        'build:Build the project and Docker images'
        'test:Run all tests'
        'check:Run all checks'
        'ktlintFormat:Format Kotlin sources with ktlint'
        'clean:Clean Gradle outputs'
        'deps:Show dependencies'
        'make-verify:Validate Make target wiring'
        'up:Start stopped services only'
        'stop:Stop running services'
        'restart:Build images and recreate services'
        'down:Remove the stack'
        'logs:Follow stack logs'
        'status:Show and validate service status'
        'pull:Pull external infrastructure images'
        'compose-config:Show merged Docker Compose configuration'
        'kafka-init-build:Build Kafka bootstrap image'
        'kafka-topics:List Kafka topics'
        'kafka-groups:List Kafka consumer groups'
        'kafka-create-<topic>:Create Kafka topic'
        'kafka-delete-<topic>:Delete Kafka topic'
        'kafka-describe-<topic>:Describe Kafka topic'
        'app-postgres-shell:Open application PostgreSQL shell'
        'keycloak-postgres-shell:Open Keycloak PostgreSQL shell'
        'keycloak-restart:Rebuild authenticator and recreate Keycloak'
        'keycloak-import:Import Keycloak configuration'
        'completion-install:Install zsh completion'
        'completion-uninstall:Remove zsh completion'
    )

    for service in "$root"/apps/*-service(N/); do
        service="${service:t}"
        service="${service%-service}"
        targets+=(
            "$service-build:Build service and Docker image"
            "$service-test:Run service tests"
            "$service-check:Run service checks"
            "$service-clean:Clean service Gradle outputs"
            "$service-deps:Show service dependencies"
            "$service-up:Start service only"
            "$service-stop:Stop service"
            "$service-restart:Build image and recreate service"
            "$service-status:Show and validate service status"
            "$service-remove:Remove service container"
            "$service-logs:Follow service logs"
            "$service-shell:Open service shell"
        )
    done

    for service in kafka kafka-init keycloak keycloak-config app-postgres keycloak-postgres redis; do
        targets+=(
            "$service-up:Start compose service"
            "$service-stop:Stop compose service"
            "$service-status:Show and validate compose service status"
            "$service-remove:Remove compose service container"
            "$service-logs:Follow compose service logs"
        )

        if [[ "$service" != "keycloak" ]]; then
            targets+=("$service-restart:Recreate compose service")
        fi
    done

    for service in kafka keycloak app-postgres keycloak-postgres redis; do
        targets+=("$service-shell:Open compose service shell")
    done

    for service in kafka keycloak keycloak-config app-postgres keycloak-postgres redis; do
        targets+=("$service-pull:Pull external infrastructure image")
    done

    _describe 'Mnemos command' targets
}

autoload -Uz compinit
compinit
compdef _mnemos_make make gmake
