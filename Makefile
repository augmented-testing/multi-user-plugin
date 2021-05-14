
JUNIT_VERSION = 4.13.2
JSON_SIMPLE_VERSION = 1.1.1

.PHONY: all
all: clean build

.PHONY: clean
clean: ## Clean up all build artifacts.
	rm -v -f plugin/*.class

.PHONY: deps
deps: ## Download all dependencies.
	mkdir -p bin
	curl -o ./bin/json-simple-$(JSON_SIMPLE_VERSION).jar https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/$(JSON_SIMPLE_VERSION)/json-simple-$(JSON_SIMPLE_VERSION).jar
	curl -o ./bin/junit-$(JUNIT_VERSION).jar https://repo1.maven.org/maven2/junit/junit/$(JUNIT_VERSION)/junit-$(JUNIT_VERSION).jar

.PHONY: build
build: ## Build all plugins.
	javac -cp "Scout.jar" ./plugin/*.java

.PHONY: run
run: build ## Run Scout with fresh build plugins.
	java -jar Scout.jar

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
