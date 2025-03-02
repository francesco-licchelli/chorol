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
## Progress
Done: 74/83
[██████████████████░░]

| Syntax node                         | Supported |
|-------------------------------------|-----------|
| ServiceNode                         | ✅         |
| DefinitionNode                      | ✅         |
| ParallelStatement                   | ✅         |
| SequenceStatement                   | ✅         |
| NDChoiceStatement                   | ✅         |
| OneWayOperationStatement            | ✅         |
| RequestResponseOperationStatement   | ✅         |
| NotificationOperationStatement      | ✅         |
| SolicitResponseOperationStatement   | ✅         |
| IfStatement                         | ✅         |
| WhileStatement                      | ✅         |
| ForEachArrayItemStatement           | ✅         |
| ForStatement                        | ✅         |
| ForEachSubNodeStatement             | ✅         |
| NullProcessStatement                | ✅         |
| SynchronizedStatement               | ✅         |
| ExitStatement                       | ✅         |
| Scope                               | ✅         |
| InstallStatement                    | ✅         |
| ThrowStatement                      | ✅         |
| DefinitionCallStatement             | ✅         |
| ProvideUntilStatement               | ✅         |
| SpawnStatement                      | ✅         |
| Program                             | ✅         |
| OneWayOperationDeclaration          | ✅         |
| RequestResponseOperationDeclaration | ✅         |
| AssignStatement                     | ✅         |
| AddAssignStatement                  | ✅         |
| SubtractAssignStatement             | ✅         |
| MultiplyAssignStatement             | ✅         |
| DivideAssignStatement               | ✅         |
| ConstantBoolExpression              | ✅         |
| ConstantLongExpression              | ✅         |
| ConstantStringExpression            | ✅         |
| DeepCopyStatement                   | ✅         |
| UndefStatement                      | ✅         |
| EmbedServiceNode                    | ✅         |
| EmbeddedServiceNode                 | ✅         |
| SolicitResponseExpressionNode       | ✅         |
| IfExpressionNode                    | ✅         |
| TypeChoiceDefinition                | ✅         |
| OrConditionNode                     | ✅         |
| AndConditionNode                    | ✅         |
| NotExpressionNode                   | ✅         |
| CompareConditionNode                | ✅         |
| ConstantIntegerExpression           | ✅         |
| ConstantDoubleExpression            | ✅         |
| ProductExpressionNode               | ✅         |
| SumExpressionNode                   | ✅         |
| VariableExpressionNode              | ✅         |
| ExecutionInfo                       | ✅         |
| InputPortInfo                       | ✅         |
| OutputPortInfo                      | ✅         |
| ValueVectorSizeExpressionNode       | ✅         |
| PreIncrementStatement               | ✅         |
| PostIncrementStatement              | ✅         |
| PreDecrementStatement               | ✅         |
| PostDecrementStatement              | ✅         |
| IsTypeExpressionNode                | ✅         |
| InstanceOfExpressionNode            | ✅         |
| TypeCastExpressionNode              | ✅         |
| InstallFixedVariableExpressionNode  | ✅         |
| InterfaceExtenderDefinition         | ✅         |
| InlineTreeExpressionNode            | ✅         |
| VoidExpressionNode                  | ✅         |
| TypeDefinitionLink                  | ✅         |
| InterfaceDefinition                 | ✅         |
| TypeInlineDefinition                | ✅         |
| FreshValueExpressionNode            | ✅         |
| DocumentationComment                | ✅         |
| ImportStatement                     | ✅         |
| LinkInStatement                     | ✅         |
| LinkOutStatement                    | ✅         |
| VariablePathNode                    | ✅         |
| CompensateStatement                 | ❌         |
| CorrelationSetInfo                  | ❌         |
| PointerStatement                    | ❌         |
| RunStatement                        | ❌         |
| CurrentHandlerStatement             | ❌         |
| CourierDefinitionNode               | ❌         |
| CourierChoiceStatement              | ❌         |
| NotificationForwardStatement        | ❌         |
| SolicitResponseForwardStatement     | ❌         |
