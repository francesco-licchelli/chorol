service Main {
    outputPort output{
        oneWay:
            op1( any ), op2( any ), op3( any )
    }
    main {
        for (elem1 in array1){
            op1@output( void )
            for (elem2 in array2){
                op2@output( void )
            }
        }
    }
}
