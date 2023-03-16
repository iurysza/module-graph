### Dependency Diagram
```mermaid
%%{
  init: {
    'theme': 'light',
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
  subgraph others
    example
    example2
  end
  example --> example2
```