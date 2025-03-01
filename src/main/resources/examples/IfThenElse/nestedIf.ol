service Main {

    outputPort output{
        oneWay:
            f1(any), f2(any)
    }


    main {
        if ( condizione1 ){
            if ( condizione2 ){ f1@output(void) }
            else if ( condizione3 ){
                if ( condizione4 ) {
                    f2@output(void)
                }
            }
        }
    }
}