service Main{
    outputPort output{
        oneWay:
            op( 1 )( any ), op( 2 )( any )
    }

    main{
        op( 1 )@output( void )
        op( 2 )@output( void )
    }
}