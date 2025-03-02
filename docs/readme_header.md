# JolieGraph

This Java application generates a graph where each node is labeled with its type. By default, only the main types are
displayed. If you need to see a more detailed view, including the subtypes of composite types, use the `-T` (or
`--full-type`) option.

## Usage

```bash
java -jar JolieGraph.jar [options]
```

## Options

- **-T, --full-type**
  When used, graph labels display types recursively,