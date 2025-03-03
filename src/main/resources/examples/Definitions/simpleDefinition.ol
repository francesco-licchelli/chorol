service Main {
  outputPort output{
        oneWay:
            op1( any )
    }
    define definition{
        op1@output( void )
    }
    main {
        definition
    }
}
