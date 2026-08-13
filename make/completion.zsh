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
        'clean:Clean Gradle outputs'
        'deps:Show dependencies'
        'up:Start stopped services only'
        'stop:Stop running services'
        'restart:Build images and recreate services'
        'down:Remove the stack'
        'logs:Follow stack logs'
        'status:Show service status'
        'compose-config:Show merged Docker Compose configuration'
        'kafka-topics:List Kafka topics'
        'kafka-groups:List Kafka consumer groups'
        'app-postgres-shell:Open application PostgreSQL shell'
        'keycloak-postgres-shell:Open Keycloak PostgreSQL shell'
        'keycloak-import:Import Keycloak configuration'
        'completion-install:Install zsh completion'
        'completion-uninstall:Remove zsh completion'
    )

    for service in "$root"/apps/*-service(N/); do
        service="${service:t}"; service="${service%-service}"
        targets+=(
            "$service-build:Build service and Docker image"
            "$service-test:Run service tests"
            "$service-check:Run service checks"
            "$service-clean:Clean service Gradle outputs"
            "$service-deps:Show service dependencies"
            "$service-up:Start service only"
            "$service-stop:Stop service"
            "$service-restart:Build image and recreate service"
            "$service-remove:Remove service container"
            "$service-logs:Follow service logs"
            "$service-shell:Open service shell"
            "$service-pull:Pull service image"
        )
    done

    _describe 'Mnemos command' targets
}

autoload -Uz compinit
compinit
compdef _mnemos_make make gmake
