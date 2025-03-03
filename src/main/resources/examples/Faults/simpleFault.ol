service Main {
    outputPort output{
        oneWay:
            op( void )
    }
    main {
        install( SampleFault => op@output( void ) )
        throw SampleFault( void )
    }
}