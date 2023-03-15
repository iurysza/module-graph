# Gradle Module Dependency Graph Plugin

[![Pre Merge Checks](https://github.com/iurysza/module-graph/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/iurysza/module-graph/actions?query=workflow%3A%22Pre+Merge+Checks%22) [![License](https://img.shields.io/github/license/cortinico/kotlin-android-template.svg)](LICENSE) ![Language](https://img.shields.io/github/languages/top/cortinico/kotlin-android-template?color=blue&logo=kotlin)

Introducing the Gradle Module Dependency Graph Plugin! 🌟

The Gradle Module Dependency Graph Plugin is a plugin that generates a [Mermaid](https://github.com/mermaid-js/mermaid) dependency graph for your Gradle project.
It provides a visual representation of your project's module dependencies, making it easier to understand the structure and relationships between modules.

## Features

- Generate a Mermaid dependency graph of your the modules in your Gradle project
- Automatically append the generated graph to your project's README file

## Getting Started

You'll just need to add it to your project's `build.gradle` or `build.gradle.kts` file.

<details>
  <summary><b>build.gradle (Groovy DSL)</b></summary>

```groovy
  createModuleGraph {
      readmeFile = file('README.md')
      heading = '## Custom Heading'
  }
```
</details>

<p></>

<details open>
  <summary><b>build.gradle.kts (Kotlin DSL)</b></summary>

```kotlin
createModuleGraph {
    readmeFile.set(file("README.md"))
    heading.set("## Custom Heading")
}
```
</details>

## Configuration

To configure the Gradle Module Dependency Graph Plugin, you can set the following options:

- `readmeFile`: The README file where the dependency graph will be appended. Default is the project's README file.
- `heading`: The heading where the dependency graph will be appended. Default is "## Module Dependency Graph".

## Usage

To generate the Mermaid dependency graph for your project, run the following command:

```sh
./gradlew createModuleGraph
```

## Sample Diagram

When ran you can expect it to generate this kind of diagram:

```mermaid
%%{
  init: {
    'theme': 'dark',
    'themeVariables': {
      'primaryColor': '#C4C7B300',
      'primaryTextColor': '#fff',
      'primaryBorderColor': '#7C0000',
      'lineColor': '#FF2F2F2F',
      'secondaryColor': '#006100',
      'tertiaryColor': '#fff'
    }
  }
}%%

graph LR
  subgraph app
    main
    playground
  end
  subgraph core
    common
    design-system
    footballdata
    reddit
  end
  subgraph features
    match-day
    match-thread
  end
  main --> match-thread
  main --> match-day
  main --> footballdata
  main --> reddit
  main --> design-system
  main --> common
  playground --> match-thread
  playground --> match-day
  playground --> footballdata
  playground --> reddit
  playground --> design-system
  playground --> common
  footballdata --> common
  reddit --> common
  match-day --> common
  match-day --> footballdata
  match-day --> design-system
  match-day --> reddit
  match-thread --> common
  match-thread --> footballdata
  match-thread --> design-system
  match-thread --> reddit
```

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This template is licensed under the MIT License - see the [License](License) file for details.
