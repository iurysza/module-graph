# Module Graph

```mermaid
%%{
  init: {
    'theme': 'base',
	'themeVariables': {"primaryTextColor":"#F6F8FAff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","tertiaryColor":"#40375c","lineColor":"#f5a623","fontSize":"12px"}
  }
}%%

graph TB
  subgraph sample
    alpha
  end
  subgraph container
    delta
  end
  alpha --> delta

classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
class delta focus
```