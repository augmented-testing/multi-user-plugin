# Multi-user Plugin for Scout

[![Plugins](https://github.com/augmented-testing/multi-user-plugin/actions/workflows/plugins.yml/badge.svg)](https://github.com/augmented-testing/multi-user-plugin/actions/workflows/plugins.yml)

## Introduction

The multi-user plugin for Scout enables collaborative testing.

The main repository for Scout can be found [here](https://github.com/augmented-testing/scout).

## Requirements

Scout is developed in Java and therefore requires the Java Runtime Environment (JRE).
JRE version 8 or later is suitable for running Scout.

## Run Scout

You can run Scout either by clicking on the `Scout.jar` or using the command `java -jar Scout.jar`.
Before you can run Scout for the first time you have to build all plugins with `make build`.

### Build and Run

Build automation is accomplished using a Makefile. To get an overview of all provided make targets run `make help`.

- To **download** all dependencies: `make deps`.
- To **build** the plugin: `make build`.
- To **build** the plugin and perform **tests**: `make test`.
- To **deploy** the plugin to an existing Scout installation: `make deploy`.

### VSCode

If you decide to use VSCode as IDE than you have to install the [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) to be able to develop a plugin.

You can find more information about how to manage Java projects in VSCode following this [link](https://code.visualstudio.com/docs/java/java-project).

### IntelliJ

If you use IntelliJ we recommend you to use the [Makefile Language](https://plugins.jetbrains.com/plugin/9333-makefile-language) extension to use the already defined build and run tasks. See the screenshot.

![IntelliJ Run Config](intellij-run-config.png)

## License

Copyright (c) 2021 Andreas Bauer

This work (source code) is licensed under [MIT](./LICENSES/MIT.txt).

Files other than source code are licensed as follows:

- Documentation and screenshots are licensed under [CC BY-SA 4.0](./LICENSES/CC-BY-SA-4.0.txt).

See the [LICENSES](./LICENSES/) folder in the root of this project for license details.
