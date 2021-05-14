.PHONY: all
all: clean build

.PHONY: clean
clean: ## Clean up all build artifacts.
	rm -v -f plugin/*.class

.PHONY: build
build: ## Build all plugins.
	javac -cp "Scout.jar" ./plugin/*.java

.PHONY: run
run: build ## Run Scout with fresh build plugins.
	java -jar Scout.jar

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
