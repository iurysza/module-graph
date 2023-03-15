# Gradle Module Dependency Graph Plugin

Introducing the Gradle Module Dependency Graph Plugin! 🌟

The Gradle Module Dependency Graph Plugin is a plugin that generates a [Mermaid](https://github.com/mermaid-js/mermaid) dependency graph for your Gradle project.
It provides a visual representation of your project's module dependencies, making it easier to understand the structure and relationships between modules.

## Features

- Generate a Mermaid dependency graph of your the modules in your Gradle project
- Automatically append the generated graph to your project's README file

## Getting Started

To use the Gradle Module Dependency Graph Plugin, you'll need to add it to your project's `build.gradle` or `build.gradle.kts` file.

### build.gradle

Add the following to your `build.gradle` file:

```groovy
plugins {
    id 'dev.iurysouza.dependency-graph' version '0.1.0'
}
```

### build.gradle.kts

Add the following to your `build.gradle.kts` file:

```kotlin
plugins {
    id("dev.iurysouza.dependency-graph") version "0.1.0"
}
```

## Configuration

To configure the Gradle Module Dependency Graph Plugin, you can set the following options:

- `readmeFile`: The README file where the dependency graph will be appended. Default is the project's README file.
- `heading`: The heading where the dependency graph will be appended. Default is "## Module Dependency Graph".

### build.gradle

```groovy
createModuleGraph {
    readmeFile = file('README.md')
    heading = '## Custom Heading'
}
```

### build.gradle.kts

```kotlin
createModuleGraph {
    readmeFile.set(file("README.md"))
    heading.set("## Custom Heading")
}
```

## Usage

To generate the Mermaid dependency graph for your project, run the following command:

```sh
./gradlew createModuleGraph
```

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This template is licensed under the MIT License - see the [License](License) file for details.
Please note that the generated template is offering to start with a MIT license but you can change it to whatever you wish, as long as you attribute under the MIT terms that you're using the template.
