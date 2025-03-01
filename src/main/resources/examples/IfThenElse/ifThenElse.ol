service Main {

    outputPort output{
        oneWay:
            f1(any), f2(any)
    }


    main {
        if ( condizione ){
            f1@output(void)
        }
        else {
            f2@output(void)
        }
    }
}
