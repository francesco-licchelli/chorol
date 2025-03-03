service Main{
    outputPort output{
        oneWay:
            op1( any )
        requestResponse:
            op2( any )
    }

    main{
        op1@output( void ) | op2@output( void )
    }
}