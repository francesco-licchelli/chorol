# ChorOl

This Java application generates a graph where each node is labeled with its type. By default, only the main types are
displayed. If you need to see a more detailed view, including the subtypes of composite types, use the `-T` (or
`--full-type`) option.

## Usage

```bash
java -jar Application.jar [options]
```

## Options

- **-T, --full-type**
  When used, graph labels display types recursively,

## Progress

Done: 16/83
[████░░░░░░░░░░░░░░░░]

| Syntax node                         | Supported |
|-------------------------------------|-----------|
| Program                             | ✅         |
| OneWayOperationDeclaration          | ❌         |
| RequestResponseOperationDeclaration | ❌         |
| DefinitionNode                      | ✅         |
| ParallelStatement                   | ✅         |
| SequenceStatement                   | ✅         |
| NDChoiceStatement                   | ✅         |
| OneWayOperationStatement            | ✅         |
| RequestResponseOperationStatement   | ✅         |
| NotificationOperationStatement      | ✅         |
| SolicitResponseOperationStatement   | ✅         |
| LinkInStatement                     | ❌         |
| LinkOutStatement                    | ❌         |
| AssignStatement                     | ❌         |
| AddAssignStatement                  | ❌         |
| SubtractAssignStatement             | ❌         |
| MultiplyAssignStatement             | ❌         |
| DivideAssignStatement               | ❌         |
| IfStatement                         | ✅         |
| DefinitionCallStatement             | ❌         |
| WhileStatement                      | ✅         |
| OrConditionNode                     | ❌         |
| AndConditionNode                    | ❌         |
| NotExpressionNode                   | ❌         |
| CompareConditionNode                | ❌         |
| ConstantIntegerExpression           | ❌         |
| ConstantDoubleExpression            | ❌         |
| ConstantBoolExpression              | ❌         |
| ConstantLongExpression              | ❌         |
| ConstantStringExpression            | ❌         |
| ProductExpressionNode               | ❌         |
| SumExpressionNode                   | ❌         |
| VariableExpressionNode              | ❌         |
| NullProcessStatement                | ✅         |
| Scope                               | ✅         |
| InstallStatement                    | ✅         |
| CompensateStatement                 | ❌         |
| ThrowStatement                      | ❌         |
| ExitStatement                       | ❌         |
| ExecutionInfo                       | ❌         |
| CorrelationSetInfo                  | ❌         |
| InputPortInfo                       | ❌         |
| OutputPortInfo                      | ❌         |
| PointerStatement                    | ❌         |
| DeepCopyStatement                   | ❌         |
| RunStatement                        | ❌         |
| UndefStatement                      | ❌         |
| ValueVectorSizeExpressionNode       | ❌         |
| PreIncrementStatement               | ❌         |
| PostIncrementStatement              | ❌         |
| PreDecrementStatement               | ❌         |
| PostDecrementStatement              | ❌         |
| ForStatement                        | ❌         |
| ForEachSubNodeStatement             | ❌         |
| ForEachArrayItemStatement           | ✅         |
| SpawnStatement                      | ❌         |
| IsTypeExpressionNode                | ❌         |
| InstanceOfExpressionNode            | ❌         |
| TypeCastExpressionNode              | ❌         |
| SynchronizedStatement               | ✅         |
| CurrentHandlerStatement             | ❌         |
| EmbeddedServiceNode                 | ❌         |
| InstallFixedVariableExpressionNode  | ❌         |
| VariablePathNode                    | ❌         |
| TypeInlineDefinition                | ❌         |
| TypeDefinitionLink                  | ❌         |
| InterfaceDefinition                 | ❌         |
| DocumentationComment                | ❌         |
| FreshValueExpressionNode            | ❌         |
| CourierDefinitionNode               | ❌         |
| CourierChoiceStatement              | ❌         |
| NotificationForwardStatement        | ❌         |
| SolicitResponseForwardStatement     | ❌         |
| InterfaceExtenderDefinition         | ❌         |
| InlineTreeExpressionNode            | ❌         |
| VoidExpressionNode                  | ❌         |
| ProvideUntilStatement               | ❌         |
| TypeChoiceDefinition                | ❌         |
| ImportStatement                     | ❌         |
| ServiceNode                         | ❌         |
| EmbedServiceNode                    | ❌         |
| SolicitResponseExpressionNode       | ❌         |
| IfExpressionNode                    | ❌         |
