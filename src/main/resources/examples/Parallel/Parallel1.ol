service Main{
    outputPort output{
        oneWay:
            op1( any ), op2( any )
    }

    main{
        op1@output( void ) | op2@output( void )
    }
}