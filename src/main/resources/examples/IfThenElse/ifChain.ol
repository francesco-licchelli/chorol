service Main {

    outputPort output{
        oneWay:
            f1(any), f2(any), f3(any), f4(any)
    }


    main {
        if ( condition1 ){
            f1@output(void)
        } else if ( condition2 ){
            f2@output(void)
        } else if ( condition3 ){
            f3@output(void)
        } else {
            f4@output(void)
        }
    }
}
