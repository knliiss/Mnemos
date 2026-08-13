.PHONY: completion-install completion-uninstall

completion-install:
	@mkdir -p "$$HOME/.mnemos"
	@cp "make/completion.zsh" "$$HOME/.mnemos/make-completion.zsh"
	@touch "$$HOME/.zshrc"
	@if ! grep -Fq 'source "$$HOME/.mnemos/make-completion.zsh"' "$$HOME/.zshrc"; then \
		printf '\n# Mnemos Make autocomplete\nsource "$$HOME/.mnemos/make-completion.zsh"\n' >> "$$HOME/.zshrc"; \
	fi
	$(call success,completion,installed — run: source ~/.zshrc)

completion-uninstall:
	@rm -f "$$HOME/.mnemos/make-completion.zsh"
	@sed -i.bak '/# Mnemos Make autocomplete/d;/source "$$HOME\/\.mnemos\/make-completion\.zsh"/d' "$$HOME/.zshrc" 2>/dev/null || true
	@rm -f "$$HOME/.zshrc.bak"
	$(call success,completion,removed)
