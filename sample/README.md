# Module Graph

```mermaid
%%{
  init: {
    'theme': 'dark'
  }
}%%

graph TB
  subgraph sample
    alpha
    beta
    zeta
  end
  subgraph container
    delta
    gama
  end
  alpha --> zeta
  alpha --> beta
  alpha --> gama
  alpha --> delta
  gama --> zeta

```
