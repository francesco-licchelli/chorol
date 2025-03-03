service Main {
  outputPort output{
        oneWay:
            op1( any )
    }
    define definition{
        op1@output( void )
    }
    define nestedDefinition{
        definition
        op1@output( void )
    }
    main {
        nestedDefinition
    }
}
