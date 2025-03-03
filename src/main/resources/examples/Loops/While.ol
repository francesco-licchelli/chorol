service Main {
    outputPort output{
        oneWay:
            op1( any )
    }
    main {
        while ( condition ) {
            op1@output( void )
        }
    }
}
