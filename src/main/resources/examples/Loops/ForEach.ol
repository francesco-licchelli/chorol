service Main {
    outputPort output{
        oneWay:
            op1( any )
    }
    main {
        for (elem in array){
            op1@output( void )
        }
    }
}
