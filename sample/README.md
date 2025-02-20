# Sample app

This is the sample app.
It has a simple module structure setup for testing out the graphs.
Some graphs will write to this file -
it's important to check they don't remove this existing content!

# 2 Build scripts?

This sample has no build scripts - one in Kotlin (kts) and one in Groovy.
This allows for testing the plugin in both languages.

Gradle only allows for 1 build script at a time,
so there should always be one script enabled, and the other should be disabled.
You can disable a build script by adding the `.x` extension to the end of the file name.
You can enable a build script by removing the `.x` extension, if present.

# Primary Graph

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {"primaryTextColor":"#F6F8FAff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","tertiaryColor":"#40375c","lineColor":"#f5a623","fontSize":"12px"}
  }
}%%

graph TB
  subgraph :sample
    :sample:zeta["zeta"]
    :sample:beta["beta"]
    :sample:alpha["alpha"]
    :sample:test["test"]
  end
  subgraph :sample:container
    :sample:container:gama["gama"]
    :sample:container:delta["delta"]
  end
  :sample:container:gama --> :sample:zeta
  :sample:zeta --> :sample:beta
  :sample:alpha --> :sample:zeta
  :sample:alpha --> :sample:beta
  :sample:alpha --> :sample:container:gama
  :sample:alpha --> :sample:container:delta
  :sample:alpha --> :sample:test

classDef java fill:#B5661C,stroke:#fff,stroke-width:2px,color:#fff;
class :sample:container:gama java
class :sample:zeta java
class :sample:beta java
class :sample:alpha java
class :sample:container:delta java
class :sample:test java

```
# Graph with root: gama

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%

graph LR

```