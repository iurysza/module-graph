# Issue 38 renderer reproduction

On 5 November 2026, the graph from [module-graph #38](https://github.com/iurysza/module-graph/issues/38)
still failed with Dagre after successful parsing. Mermaid 12.3.0's default ELK layout rendered it.
This is a workaround verification, not evidence that [Mermaid #1753](https://github.com/mermaid-js/mermaid/issues/1753) is fixed.

## Source and method

The source baseline was main at `1e9e071591c729fc44aeb334ef2de5a540ef2f21`.
The complete Mermaid block in issue #38 contains 34 nodes, 110 edges, and 9 subgraphs.
Its ordered edges were supplied to the unchanged
[`Mermaid.generateGraph`](../../plugin-build/modulegraph/src/main/kotlin/dev/iurysouza/modulegraph/Mermaid.kt)
through `GraphParseResult`, with `focusedModulesRegex = ".*(data).*"` and default grouping.
The generated graph matched the issue block exactly except for its final newline.
A second generation used `showFullPath = true`. Both generations kept the default `nestingEnabled = false`.

The renderer ran in HeadlessChrome 157.0.0.0 on macOS, through `agent-browser`.
Installed versions were Mermaid 12.3.0, `dagre-d3-es` 7.0.14, and `elkjs` 0.12.1.
The full Mermaid browser bundle supplied both layout engines. The plugin itself has no Mermaid runtime dependency.
The deployed renderer versions of GitHub and other Markdown viewers were not checked.

Commands used in a disposable directory included:

```sh
gh issue view 38 --repo iurysza/module-graph --json title,body,comments,state,url
npm install --ignore-scripts --no-audit --no-fund mermaid@12.3.0
python3 -I run-generator.py
python3 -I -m http.server 48139 --bind 127.0.0.1
agent-browser --session oss-module-graph-38 open http://127.0.0.1:48139/
agent-browser --session oss-module-graph-38 eval --stdin < render-matrix.js
```

`run-generator.py` and `render-matrix.js` were temporary reproduction scripts, not repository tools.
The first compiled and executed the unchanged Kotlin graph sources with the ordered issue edges.
The second loaded the Mermaid ESM browser bundle, called `mermaid.parse`, then awaited `mermaid.render`
for each configuration below and inspected the resulting SVG. No raw artifacts or screenshots are included here.

## Full-render results

Every case passed syntax parsing. These are full browser rendering results:

| Input | Layout configuration | Result |
| --- | --- | --- |
| Exact issue graph and current plugin output | Mermaid defaults | ELK succeeds |
| Exact issue graph and current plugin output | Host sets `layout: 'dagre'` | Dagre fails |
| Current plugin output with `showFullPath=true` | Host sets `layout: 'dagre'` | Dagre succeeds, no subgraphs |
| Exact issue graph with legacy `flowchart.defaultRenderer: 'elk'` directive | Host sets `layout: 'dagre'` | Legacy setting ignored, Dagre fails |
| Exact issue graph and current plugin output with `init.layout: 'elk'` directive | Host sets `layout: 'dagre'` | Directive selects ELK, rendering succeeds |

Successful renders contained all 34 nodes and 110 edge paths, with nonempty nodes and finite, positive SVG bounds.
ELK preserved all 9 subgraphs. The full-path option removed them.
The Dagre error was `TypeError: Cannot set properties of undefined (setting 'order')`.
Its stack reached `assignOrder` in `dagre-d3-es/src/dagre/order/index.js` at `g.node(v).order = i`.

The tested modern directive was:

```text
%%{init: {'theme': 'neutral', 'layout': 'elk'}}%%
```

## Coverage and implications

The generator ran with cached Kotlin 2.4.20, JBR 21, Gradle 9.7.1 API jars, and serialization 1.12.0,
targeting JVM 1.8. The source baseline declares Kotlin 2.4.21 and Gradle 9.8.2.
That reproduction was source-level graph generation, not a full Gradle build. It did not exercise Gradle project discovery
or rebuild Tivi. It establishes output equivalence and renderer behavior only.

Subsequent documentation validation ran `./gradlew preMerge --continue --no-daemon` in the worktree and exited 0.
Gradle 9.8.2 ran on JBR 21.0.6, with task-local Zulu JDK 8u512 and 17.0.21.0.3 toolchains from Azul's official distribution.
`GRADLE_USER_HOME` and the extracted toolchains were under a disposable task directory, with automatic toolchain download disabled.
The gate passed sample checks, plugin checks, and plugin validation. This does not establish compatibility with every Mermaid host.

The existing `showFullPath` option is adequate for this reproducer if removing subgraphs is acceptable.
[`FlatSubgraphBuilder`](../../plugin-build/modulegraph/src/main/kotlin/dev/iurysouza/modulegraph/graph/subgraph/FlatSubgraphBuilder.kt)
omits grouping when that option is true.
[`ReadmeWriter`](../../plugin-build/modulegraph/src/main/kotlin/dev/iurysouza/modulegraph/ReadmeWriter.kt)
replaces the generated section on each run, so manual renderer directives do not persist across regeneration.
The [README troubleshooting section](../../README.md#troubleshooting) documents both limitations.
No new plugin API or default change is justified by this upstream renderer failure.
